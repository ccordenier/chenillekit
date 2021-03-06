/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2008-2011 by chenillekit.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package org.chenillekit.quartz;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.tapestry5.ioc.annotations.EagerLoad;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;

import org.chenillekit.quartz.services.JobSchedulingBundle;
import org.chenillekit.quartz.services.QuartzSchedulerManager;
import org.chenillekit.quartz.services.impl.QuartzSchedulerManagerImpl;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;

/**
 * @version $Id$
 */
public class ChenilleKitQuartzModule
{
    /**
     * bind the <a href="http://www.opensymphony.com/quartz/">Quartz</a> scheduler factory.
     * <p/>
     * first we look for configuration contribution. if not exists (is null) we try to access
     * the quartz.properties in classpath.
     *
     * @param shutdownHub the shutdown hub
     *
     * @return scheduler factory
     */
    public SchedulerFactory buildSchedulerFactory(Logger logger,
                                                         RegistryShutdownHub shutdownHub,
                                                         List<URL> contributions)
    {
        if (logger.isInfoEnabled())
            logger.info("initialize scheduler factory");

        try
        {
        	
        	if (contributions.isEmpty())
        		throw new RuntimeException("Configuration to SchedulerFactory services needed");
        	
        	Properties prop = new Properties();
        	
        	for (URL contibutionURL: contributions)
        	{
        		prop.load(contibutionURL.openStream());
			}
        	
            final SchedulerFactory factory = new StdSchedulerFactory(prop);

            shutdownHub.addRegistryShutdownListener(new Runnable()
            {
				/**
				 * When an object implementing interface <code>Runnable</code> is used
				 * to create a thread, starting the thread causes the object's
				 * <code>run</code> method to be called in that separately executing
				 * thread.
				 * <p/>
				 * The general contract of the method <code>run</code> is that it may
				 * take any action whatsoever.
				 *
				 * @see Thread#run()
				 */
				public void run()
				{
					try
					{
						List<Scheduler> schedulers = CollectionFactory.newList(factory.getAllSchedulers());
						for (Scheduler scheduler : schedulers)
							scheduler.shutdown();
					}
					catch (SchedulerException e)
					{
						throw new RuntimeException(e);
					}
				}

            });

            return factory;
        }
        catch (IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
        catch (SchedulerException se)
        {
            throw new RuntimeException(se);
        }
    }

    /**
     * bind the <a href="http://www.opensymphony.com/quartz/">Quartz</a> based scheduler manager.
     *
     * @param schedulerFactory     the scheduler factory
     * @param jobSchedulingBundles list of job detail and trigger bundles
     *
     * @return scheduler manager
     */
    @EagerLoad
    public QuartzSchedulerManager buildQuartzSchedulerManager(Logger logger, final SchedulerFactory schedulerFactory,
                                                                     final List<JobSchedulingBundle> jobSchedulingBundles)
    {
        return new QuartzSchedulerManagerImpl(logger, schedulerFactory, jobSchedulingBundles);
    }
}
