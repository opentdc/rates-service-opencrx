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

import java.util.List;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.openmdx.base.exception.ServiceException;
import org.opentdc.opencrx.AbstractOpencrxServiceProvider;
import org.opentdc.rates.RatesModel;
import org.opentdc.rates.ServiceProvider;
import org.opentdc.service.exception.DuplicateException;
import org.opentdc.service.exception.NotFoundException;

public class OpencrxServiceProvider extends AbstractOpencrxServiceProvider implements ServiceProvider {
	
	public static final short ACTIVITY_GROUP_TYPE_PROJECT = 40;
	public static final short ACCOUNT_ROLE_CUSTOMER = 100;
	public static final short ACTIVITY_CLASS_INCIDENT = 2;
	public static final short ICAL_TYPE_NA = 0;
	public static final short ICAL_CLASS_NA = 0;
	public static final short ICAL_TYPE_VEVENT = 1;

	// instance variables
	// private static final Logger logger = Logger.getLogger(OpencrxServiceProvider.class.getName());

	public OpencrxServiceProvider(
		ServletContext context, 
		String prefix
	) throws ServiceException, NamingException {
		super(context, prefix);
	}

	@Override
	public List<RatesModel> list(String queryType, String query, long position,
			long size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RatesModel create(RatesModel rate) throws DuplicateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RatesModel read(String id) throws NotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RatesModel update(String id, RatesModel rate)
			throws NotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(String id) throws NotFoundException {
		// TODO Auto-generated method stub
		
	}
}
