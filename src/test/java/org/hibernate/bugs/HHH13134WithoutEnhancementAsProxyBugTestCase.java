/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.bugs;

import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.test.*;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class HHH13134WithoutEnhancementAsProxyBugTestCase extends BaseCoreFunctionalTestCase {

    private boolean inserted = false;

    @Override
    protected Class[] getAnnotatedClasses() {
        return new Class[]{
                MessageWithLazyToOne.class,
                MessageWithoutLazyToOne.class,
                Patient.class,
                Practitioner.class,
                User.class,
        };
    }

    @Override
    protected String getBaseForMappings() {
        return "org/hibernate/test/";
    }

    // Add in any settings that are specific to your test.  See resources/hibernate.properties for the defaults.
    @Override
    protected void configure(Configuration configuration) {
        super.configure(configuration);

        configuration.setProperty(AvailableSettings.SHOW_SQL, Boolean.TRUE.toString());
        configuration.setProperty(AvailableSettings.FORMAT_SQL, Boolean.TRUE.toString());
        //configuration.setProperty( AvailableSettings.GENERATE_STATISTICS, "true" );

        // disable enhancement as proxy
        configuration.setProperty(AvailableSettings.ALLOW_ENHANCEMENT_AS_PROXY, Boolean.FALSE.toString());
    }

    @Before
    public void setup() {

        if (inserted)
            return;

        Session s = openSession();
        Transaction tx = s.beginTransaction();

        long i = 1;

        Practitioner practitioner = new Practitioner()
                .setId(i);
        s.persist(practitioner);

        User user = new User()
                .setLogin("login" + i)
                .setName("John")
                .setPractitioner(practitioner);
        s.persist(user);

        Patient p = new Patient()
                .setId(i)
                .setName("Jane")
                .addPractitioner(practitioner);
        s.persist(p);

        for (long j = 0; j < 5; j++) {

            MessageWithoutLazyToOne mwo = new MessageWithoutLazyToOne()
                    .setId(j)
                    .setPatient(p)
                    .setPractitioner(practitioner);
            s.persist(mwo);

            MessageWithLazyToOne mw = new MessageWithLazyToOne()
                    .setId(j)
                    .setPatient(p)
                    .setPractitioner(practitioner);
            s.persist(mw);

        }

        tx.commit();
        s.close();

        inserted = true;
    }

    @Test
    public void multiple_occurrences_fail() {

        Session s = openSession();
        Transaction tx = s.beginTransaction();

        log.info("select...");
        List<MessageWithoutLazyToOne> list = s.createQuery(
                "SELECT m FROM MessageWithoutLazyToOne m ",
                MessageWithoutLazyToOne.class).list();
        log.info(list.size() + " results");

        tx.commit();
        s.close();

        for (MessageWithoutLazyToOne m : list) {
            final Patient p = m.getPatient();
            Assert.assertThrows(LazyInitializationException.class, p::getName);
        }
    }

    @Test
    public void one_occurrence_pass() {

        Session s = openSession();
        Transaction tx = s.beginTransaction();

        log.info("select...");
        List<MessageWithoutLazyToOne> list = s.createQuery(
                "SELECT m FROM MessageWithoutLazyToOne m where m.id = 1",
                MessageWithoutLazyToOne.class).list();
        log.info(list.size() + " results");

        tx.commit();
        s.close();

        for (MessageWithoutLazyToOne m : list) {
            final Patient p = m.getPatient();
            Assert.assertThrows(LazyInitializationException.class, p::getName);
        }
    }
}
