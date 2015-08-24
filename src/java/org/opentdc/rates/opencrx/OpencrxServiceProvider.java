/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Arbalo AG
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.opentdc.rates.opencrx;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.opencrx.kernel.activity1.cci2.ResourceRateQuery;
import org.opencrx.kernel.activity1.jmi1.Resource;
import org.opencrx.kernel.activity1.jmi1.ResourceRate;
import org.opencrx.kernel.utils.Utils;
import org.openmdx.base.exception.ServiceException;
import org.opentdc.opencrx.AbstractOpencrxServiceProvider;
import org.opentdc.opencrx.ActivitiesHelper;
import org.opentdc.rates.Currency;
import org.opentdc.rates.RateType;
import org.opentdc.rates.RateModel;
import org.opentdc.rates.ServiceProvider;
import org.opentdc.service.exception.DuplicateException;
import org.opentdc.service.exception.InternalServerErrorException;
import org.opentdc.service.exception.NotFoundException;
import org.opentdc.service.exception.ValidationException;

/**
 * Rates service for openCRX.
 *
 */
public class OpencrxServiceProvider extends AbstractOpencrxServiceProvider implements ServiceProvider {

	private static final Logger logger = Logger.getLogger(OpencrxServiceProvider.class.getName());

	/**
	 * Constructor.
	 * 
	 * @param context the servlet context
	 * @param prefix the simple class name of the service provider
	 * @throws ServiceException
	 * @throws NamingException
	 */
	public OpencrxServiceProvider(
		ServletContext context, 
		String prefix
	) throws ServiceException, NamingException {
		super(context, prefix);
	}

	/**
	 * Get RateType to rate type code.
	 * 
	 * @param rateType
	 * @return
	 */
	protected short toRateTypeCode(
		RateType rateType
	) {
		switch(rateType) {
			case STANDARD_INTERNAL:
				return 100;
			case STANDARD_EXTERNAL_ON_SITE:
				return 101;
			case STANDARD_EXTERNAL_OFF_SITE:
				return 102;
			case OVERTIME_INTERNAL:
				return 103;
			case OVERTIME_EXTERNAL_ON_SITE:
				return 104;
			case OVERTIME_EXTERNAL_OFF_SITE:
				return 105;
			default:
				return 0;
		}
	}

	/**
	 * Map rate type code to RateType.
	 * 
	 * @param rateTypeCode
	 * @return
	 */
	protected RateType toRateType(
		short rateTypeCode
	) {
		if(rateTypeCode == 100) {
			return RateType.STANDARD_INTERNAL;
		} else if(rateTypeCode == 101) {
			return RateType.STANDARD_EXTERNAL_ON_SITE;
		} else if(rateTypeCode == 102) {
			return RateType.STANDARD_EXTERNAL_OFF_SITE;
		} else if(rateTypeCode == 103) {
			return RateType.OVERTIME_INTERNAL;
		} else if(rateTypeCode == 104) {
			return RateType.OVERTIME_EXTERNAL_ON_SITE;
		} else if(rateTypeCode == 105) {
			return RateType.OVERTIME_EXTERNAL_OFF_SITE;
		} else {
			return RateType.getDefaultRateType();
		}
	}

	/**
	 * Map resource rate to rate.
	 * 
	 * @param resourceRate
	 * @return
	 */
	protected RateModel mapToRate(
		ResourceRate resourceRate
	) {
		RateModel rates = new RateModel();
		rates.setCreatedAt(resourceRate.getCreatedAt());
		rates.setCreatedBy(resourceRate.getCreatedBy().get(0));
		rates.setModifiedAt(resourceRate.getModifiedAt());
		rates.setModifiedBy(resourceRate.getModifiedBy().get(0));
		rates.setId(resourceRate.refGetPath().getLastSegment().toClassicRepresentation());
		rates.setCurrency(Currency.toCurrency(resourceRate.getRateCurrency()));
		rates.setRate(resourceRate.getRate().intValue());
		rates.setTitle(resourceRate.getName());
		rates.setDescription(resourceRate.getDescription());
		rates.setType(this.toRateType(resourceRate.getRateType()));
		return rates;
	}

	/* (non-Javadoc)
	 * @see org.opentdc.rates.ServiceProvider#list(java.lang.String, java.lang.String, long, long)
	 */
	@Override
	public List<RateModel> list(
		String query, 
		String queryType, 
		int position,
		int size
	) {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.activity1.jmi1.Segment activitySegment = this.getActivitySegment();
		Resource ratesResource = ActivitiesHelper.findRatesResource(activitySegment);
		if(ratesResource == null) {
			throw new org.opentdc.service.exception.NotFoundException(ActivitiesHelper.RATES_RESOURCE_NAME);
		} else {
			try {
				ResourceRateQuery resourceRateQuery = (ResourceRateQuery)pm.newQuery(ResourceRate.class);
				resourceRateQuery.forAllDisabled().isFalse();
				resourceRateQuery.orderByName().ascending();
				List<ResourceRate> resourceRates = ratesResource.getResourceRate(resourceRateQuery);
				List<RateModel> result = new ArrayList<RateModel>();
				int count = 0;
				for(Iterator<ResourceRate> i = resourceRates.listIterator(position); i.hasNext(); ) {
					ResourceRate resourceRate = i.next();
					result.add(this.mapToRate(resourceRate));
					count++;
					if(count >= size) {
						break;
					}
				}
				return result;
			} catch(Exception e) {
				new ServiceException(e).log();
				throw new InternalServerErrorException(e.getMessage());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opentdc.rates.ServiceProvider#create(org.opentdc.rates.RatesModel)
	 */
	@Override
	public RateModel create(
		HttpServletRequest request,
		RateModel rate
	) throws DuplicateException, ValidationException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.activity1.jmi1.Segment activitySegment = this.getActivitySegment();
		Resource ratesResource = ActivitiesHelper.findRatesResource(activitySegment);
		if(ratesResource == null) {
			throw new org.opentdc.service.exception.NotFoundException(ActivitiesHelper.RATES_RESOURCE_NAME);
		} else {
			if(rate.getId() != null) {
				ResourceRate resourceRate = null;
				try {
					resourceRate = ratesResource.getResourceRate(rate.getId());
				} catch(Exception ignore) {}
				if(resourceRate != null) {
					throw new DuplicateException("Rate with ID " + rate.getId() + " exists already.");			
				} else {
					throw new ValidationException("Rate <" + rate.getId() + "> contains an ID generated on the client. This is not allowed.");
				}
			}
			if (rate.getTitle() == null || rate.getTitle().isEmpty()) {
				throw new ValidationException("rate must contain a valid title.");
			}
			if (rate.getRate() < 0) {
				throw new ValidationException("rate: negative rates are not allowed.");
			}
			if(rate.getCurrency() == null) {
				rate.setCurrency(Currency.getDefaultCurrency());
			}
			if(rate.getType() == null) {
				rate.setType(RateType.getDefaultRateType());
			}
			ResourceRate resourceRate = null;
			resourceRate = pm.newInstance(ResourceRate.class);
			resourceRate.setRateCurrency((short)rate.getCurrency().getIsoCode());
			resourceRate.setRate(new BigDecimal(rate.getRate()));
			resourceRate.setName(rate.getTitle());
			resourceRate.setDescription(rate.getDescription());
			resourceRate.setRateType(this.toRateTypeCode(rate.getType()));
			try {
				pm.currentTransaction().begin();
				ratesResource.addResourceRate(
					Utils.getUidAsString(),
					resourceRate
				);
				pm.currentTransaction().commit();
				return this.read(
					resourceRate.refGetPath().getLastSegment().toClassicRepresentation()
				);
			} catch(Exception e) {
				new ServiceException(e).log();
				try {
					pm.currentTransaction().rollback();
				} catch(Exception ignore) {}
				throw new InternalServerErrorException("Unable to create rate");
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opentdc.rates.ServiceProvider#read(java.lang.String)
	 */
	@Override
	public RateModel read(
		String id
	) throws NotFoundException {
		org.opencrx.kernel.activity1.jmi1.Segment activitySegment = this.getActivitySegment();
		Resource ratesResource = ActivitiesHelper.findRatesResource(activitySegment);
		if(ratesResource == null) {
			throw new org.opentdc.service.exception.NotFoundException(ActivitiesHelper.RATES_RESOURCE_NAME);
		} else {
			ResourceRate resourceRate = ratesResource.getResourceRate(id);
			if(resourceRate == null || Boolean.TRUE.equals(resourceRate.isDisabled())) {
				throw new org.opentdc.service.exception.NotFoundException(id);				
			}
			return this.mapToRate(resourceRate);
		}
	}

	/* (non-Javadoc)
	 * @see org.opentdc.rates.ServiceProvider#update(java.lang.String, org.opentdc.rates.RatesModel)
	 */
	@Override
	public RateModel update(
		HttpServletRequest request,
		String id, 
		RateModel rate
	) throws NotFoundException, ValidationException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.activity1.jmi1.Segment activitySegment = this.getActivitySegment();
		Resource ratesResource = ActivitiesHelper.findRatesResource(activitySegment);
		if(ratesResource == null) {
			throw new org.opentdc.service.exception.NotFoundException(id);
		} else {
			ResourceRate resourceRate = ratesResource.getResourceRate(id);
			if(resourceRate == null || Boolean.TRUE.equals(resourceRate.isDisabled())) {
				throw new org.opentdc.service.exception.NotFoundException(id);				
			} else {
				try {
					pm.currentTransaction().begin();
					resourceRate.setRateCurrency((short)rate.getCurrency().getIsoCode());
					resourceRate.setRate(new BigDecimal(rate.getRate()));
					resourceRate.setName(rate.getTitle());
					resourceRate.setDescription(rate.getDescription());
					resourceRate.setRateType(this.toRateTypeCode(rate.getType()));
					pm.currentTransaction().commit();
				} catch(Exception e) {
					new ServiceException(e).log();
					try {
						pm.currentTransaction().rollback();
					} catch(Exception ignore) {}
					throw new InternalServerErrorException("Unable to update rate");
				}
				return this.read(id);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opentdc.rates.ServiceProvider#delete(java.lang.String)
	 */
	@Override
	public void delete(
		String id
	) throws NotFoundException, InternalServerErrorException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.activity1.jmi1.Segment activitySegment = this.getActivitySegment();
		Resource ratesResource = ActivitiesHelper.findRatesResource(activitySegment);
		if(ratesResource == null) {
			throw new org.opentdc.service.exception.NotFoundException(ActivitiesHelper.RATES_RESOURCE_NAME);
		} else {
			ResourceRate resourceRate = ratesResource.getResourceRate(id);
			if(resourceRate == null || Boolean.TRUE.equals(resourceRate.isDisabled())) {
				throw new org.opentdc.service.exception.NotFoundException(id);				
			}
			try {
				pm.currentTransaction().begin();
				resourceRate.setDisabled(true);
				pm.currentTransaction().commit();
			} catch(Exception e) {
				new ServiceException(e).log();
				try {
					pm.currentTransaction().rollback();
				} catch(Exception ignore) {}
				throw new InternalServerErrorException("Unable to delete rate");
			}
		}
	}
	
}
