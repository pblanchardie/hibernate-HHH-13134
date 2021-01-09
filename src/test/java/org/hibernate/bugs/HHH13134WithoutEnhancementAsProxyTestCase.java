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

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.test.*;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * Without enhancement as proxy, join fetch does not work
 */
public class HHH13134WithoutEnhancementAsProxyTestCase extends BaseCoreFunctionalTestCase {

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
    }

    @Before
    public void setup() {

        if(inserted)
            return;

        Session s = openSession();
        Transaction tx = s.beginTransaction();

        for (long i = 0; i < 5; i++) {

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

            MessageWithoutLazyToOne mwo = new MessageWithoutLazyToOne()
                    .setId(i)
                    .setPatient(p)
                    .setPractitioner(practitioner);
            s.persist(mwo);

            MessageWithLazyToOne mw = new MessageWithLazyToOne()
                    .setId(i)
                    .setPatient(p)
                    .setPractitioner(practitioner);
            s.persist(mw);

        }

        tx.commit();
        s.close();

        inserted = true;
    }

    @Test
    public void hhh13134_with_LazyToOne() {

        Session s = openSession();
        Transaction tx = s.beginTransaction();

        log.info("Find MessageWithLazyToOne...");
        MessageWithLazyToOne m = s.find(MessageWithLazyToOne.class, 1L);
        // LazyToOne are not included:
        //    select
        //           message0_.id as id1_0_0_
        //       from
        //           Message message0_
        //       where
        //           message0_.id=?
        log.info("Getting Message.patient.id...");
        m.getPatient().getId();
        // the two LazyToOne joinColumns (implicit LazyGroup) are selected:
        //    select
        //        messagewit_.patient_id as patient_2_0_,
        //        messagewit_.practitioner_id as practiti3_0_
        //    from
        //        MessageWithLazyToOne messagewit_
        //    where
        //        messagewit_.id=?
        // the associated Patient is loaded:
        //    select
        //        patient0_.id as id1_2_0_
        //        patient0_.name as name2_2_0_
        //    from
        //        Patient patient0_
        //    where
        //        patient0_.id=?
        // the associated Practitioner is loaded without the LazyToOne User:
        //    select
        //        practition0_.id as id1_4_0_
        //    from
        //        Practitioner practition0_
        //    where
        //        practition0_.id=?
        log.info("Getting Message.practitioner.user.login...");
        m.getPractitioner().getUser().getLogin();
        // selecting join column
        //    select
        //        practition_.user_login as user_log2_4_
        //    from
        //        Practitioner practition_
        //    where
        //        practition_.id=?
        // then loading user
        //    select
        //        user0_.login as login1_5_0_,
        //        user0_.name as name2_5_0_
        //    from
        //        User user0_
        //    where
        //        user0_.login=?
        log.info("Getting Message.practitioner.user.name...");
        m.getPractitioner().getUser().getName();
        // no more query
        tx.commit();
        s.close();
    }

    @Test
    public void hhh13134_with_LazyToOne_join_fetch() {

        Session s = openSession();
        Transaction tx = s.beginTransaction();

        log.info("selecting a MessageWithLazyToOne with join fetch...");
        MessageWithLazyToOne m = s.createQuery(
                "SELECT m " +
                        "FROM MessageWithLazyToOne m " +
                        "JOIN FETCH m.patient " +
                        "JOIN FETCH m.practitioner practitioner " +
                        "JOIN FETCH practitioner.user " +
                        "WHERE m.id = 1L",
                MessageWithLazyToOne.class).getSingleResult();
        // join columns are not included in the select list as explained in
        // https://stackoverflow.com/questions/62392245/entitygraph-join-fetch-are-not-working-with-bytecode-enanhcement/65389870#65389870
        //    select
        //        messagewit0_.id as id1_0_0_,
        //        patient1_.id as id1_2_1_,
        //        practition2_.id as id1_4_2_,
        //        user3_.login as login1_5_3_,
        //        patient1_.name as name2_2_1_,
        //        user3_.name as name2_5_3_
        //    from
        //        MessageWithLazyToOne messagewit0_
        //    inner join
        //        Patient patient1_
        //            on messagewit0_.patient_id=patient1_.id
        //    inner join
        //        Practitioner practition2_
        //            on messagewit0_.practitioner_id=practition2_.id
        //    inner join
        //        User user3_
        //            on practition2_.user_login=user3_.login
        //    where
        //        messagewit0_.id=1
        log.info("Getting Message.patient.id...");
        m.getPatient().getId();
        // one more to select the join columns that should have been selected in the initial query with join fetch, in my opinion
        //    select
        //        messagewit_.patient_id as patient_2_0_,
        //        messagewit_.practitioner_id as practiti3_0_
        //    from
        //        MessageWithLazyToOne messagewit_
        //    where
        //        messagewit_.id=?
        log.info("Getting Message.practitioner.user.login...");
        m.getPractitioner().getUser().getLogin();
        // one more select for the join column...
        //   select
        //        practition_.user_login as user_log2_4_
        //    from
        //        Practitioner practition_
        //    where
        //        practition_.id=?
        log.info("Getting Message.practitioner.user.name...");
        m.getPractitioner().getUser().getName();
        // no more query, user has been fetched on the first
        tx.commit();
        s.close();
    }

    @Test
    public void hhh13134_without_LazyToOne() {

        Session s = openSession();
        Transaction tx = s.beginTransaction();

        log.info("Find MessageWithoutLazyToOne...");
        MessageWithoutLazyToOne m = s.find(MessageWithoutLazyToOne.class, 1L);
        // The Message is selected with all non-@Lazy columns
        //    select
        //        messagewit0_.id as id1_1_0_,
        //        messagewit0_.patient_id as patient_2_1_0_,
        //        messagewit0_.practitioner_id as practiti3_1_0_
        //    from
        //        MessageWithoutLazyToOne messagewit0_
        //    where
        //        messagewit0_.id=?
        // And as documented, without @LazyToOne, all ToOne are eagerly fetched (fetch=LAZY is ignored, as with if LazyToOneOption.FALSE were used)
        //    select
        //        patient0_.id as id1_2_0_
        //        patient0_.name as name2_2_0_
        //    from
        //        Patient patient0_
        //    where
        //        patient0_.id=?
        //    select
        //        practition0_.id as id1_4_0_
        //    from
        //        Practitioner practition0_
        //    where
        //        practition0_.id=?
        log.info("Getting Message.patient.id...");
        m.getPatient().getId();
        // no more query we have the id
        log.info("Getting Message.patient.name...");
        m.getPatient().getName();
        // no more query as everything is eagerly fetched
        tx.commit();
        s.close();

    }

    @Test
    public void hhh13134_without_LazyToOne_join_fetch() {

        Session s = openSession();
        Transaction tx = s.beginTransaction();

        log.info("selecting a MessageWithoutLazyToOne with join fetch 2 associations...");
        MessageWithoutLazyToOne m = s.createQuery(
                "SELECT m " +
                        "FROM MessageWithoutLazyToOne m " +
                        "JOIN FETCH m.patient " +
                        "JOIN FETCH m.practitioner " +
                        "WHERE m.id = 1L",
                MessageWithoutLazyToOne.class).getSingleResult();
        // Everything's ok:
        // select
        //        messagewit0_.id as id1_1_0_,
        //        patient1_.id as id1_2_1_,
        //        practition2_.id as id1_4_2_,
        //        messagewit0_.patient_id as patient_2_1_0_,
        //        messagewit0_.practitioner_id as practiti3_1_0_
        //        patient1_.name as name2_2_1_
        //    from
        //        MessageWithoutLazyToOne messagewit0_
        //    inner join
        //        Patient patient1_
        //            on messagewit0_.patient_id=patient1_.id
        //    inner join
        //        Practitioner practition2_
        //            on messagewit0_.practitioner_id=practition2_.id
        //    where
        //        messagewit0_.id=1
        log.info("Getting Message.patient.id...");
        m.getPatient().getId();
        // no more query as join fetch worked this time
        log.info("Getting Message.practitioner.user.login...");
        m.getPractitioner().getUser().getLogin();
        // getting lazy stuff...
        // Hibernate:
        //    select
        //        practition_.user_login as user_log2_4_
        //    from
        //        Practitioner practition_
        //    where
        //        practition_.id=?
        // N+1 not really needed
        //    select
        //        user0_.login as login1_5_0_
        //    from
        //        User user0_
        //    where
        //        user0_.login=?
        log.info("Getting Message.practitioner.user.name...");
        m.getPractitioner().getUser().getName();
        tx.commit();
        s.close();
    }
}
