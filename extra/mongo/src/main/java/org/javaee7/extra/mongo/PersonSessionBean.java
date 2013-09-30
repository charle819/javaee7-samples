/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.javaee7.extra.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Arun Gupta
 */
@Named
@Stateless
public class PersonSessionBean {

    @Inject
    Person person;

    DBCollection personCollection;

    private MongodExecutable mongodExe;
    private MongodProcess mongod;
    private final int MONGO_PORT = 12345;

    @PostConstruct
    private void initDB() {
        try {
            // Start embedded Mongo
            MongodStarter runtime = MongodStarter.getDefaultInstance();
            mongodExe = runtime.prepare(new MongodConfigBuilder()
                    .version(Version.Main.PRODUCTION)
                    .net(new Net(MONGO_PORT, Network.localhostIsIPv6()))
                    .build());
            mongod = mongodExe.start();
                        
            // Get an instance of Mongo
            Mongo m = new Mongo("localhost", MONGO_PORT);
            DB db = m.getDB("personDB");
            personCollection = db.getCollection("persons");
            if (personCollection == null) {
                personCollection = db.createCollection("persons", null);
            }
        } catch (MongoException | IOException ex) {
            Logger.getLogger(PersonSessionBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @PreDestroy
    private void stopDB() {
        mongod.stop();
        mongodExe.stop();
    }

    public void createPerson() {
        BasicDBObject doc = person.toDBObject();
        personCollection.insert(doc);
    }

    public List<Person> getPersons() {
        List<Person> persons = new ArrayList();
        DBCursor cur = personCollection.find();
        System.out.println("getPersons: Found " + cur.length() + " person(s)");
        for (DBObject dbo : cur.toArray()) {
            persons.add(Person.fromDBObject(dbo));
        }

        return persons;
    }
}
