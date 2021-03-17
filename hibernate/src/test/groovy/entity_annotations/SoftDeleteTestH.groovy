/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package entity_annotations

import io.jmix.core.DataManager
import io.jmix.core.TimeSource
import io.jmix.core.entity.EntityEntrySoftDelete
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SystemAuthenticator
import io.jmix.hibernate.impl.types.uuid.JmixUUIDTypeDescriptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import spock.lang.Ignore
import test_support.DataSpec
import test_support.entity.soft_delete.*

class SoftDeleteTestH extends DataSpec {

    @Autowired
    DataManager dataManager

    @Autowired
    TimeSource timeSource

    @Autowired
    SystemAuthenticator authenticator

    @Autowired
    InMemoryUserRepository userRepository

    @Autowired
    JdbcTemplate jdbcTemplate;

    UserDetails admin

    def setup() {
        admin = User.builder()
                .username('admin')
                .password('{noop}admin123')
                .authorities(Collections.emptyList())
                .build()
        userRepository.addUser(admin)
        authenticator.begin()
    }

    def cleanup() {
        authenticator.end()
        userRepository.removeUser(admin)
    }

    def "Enhancing should work for SoftDelete entities"() {
        setup:
        SoftDeleteWithUserEntity entity = dataManager.create(SoftDeleteWithUserEntity)

        expect:
        (entity.__getEntityEntry()) instanceof EntityEntrySoftDelete


        when:
        EntityEntrySoftDelete softDeleteEntry = (EntityEntrySoftDelete) entity.__getEntityEntry()

        Date beforeDelete = timeSource.currentTimestamp()
        softDeleteEntry.setDeletedDate(timeSource.currentTimestamp())
        Date afterDelete = timeSource.currentTimestamp()

        softDeleteEntry.setDeletedBy("UFO")

        then:
        beforeOrEquals(beforeDelete, entity.getTimeOfDeletion())
        afterOrEquals(afterDelete, entity.getTimeOfDeletion())
        "UFO".equals(entity.getWhoDeleted())
        ((EntityEntrySoftDelete) entity.__getEntityEntry()).isDeleted()
    }


    @Ignore
    def "Soft deletion should work"() {
        setup:
        authenticator.begin("admin")

        SoftDeleteWithUserEntity entity = dataManager.save(dataManager.create(SoftDeleteWithUserEntity))
        SoftDeleteEntity tsOnly = dataManager.save(dataManager.create(SoftDeleteEntity))


        when:
        Date beforeDelete = timeSource.currentTimestamp()
        dataManager.remove(entity)
        entity = dataManager.load(SoftDeleteWithUserEntity).id(entity.getId()).softDeletion(false).one()

        dataManager.remove(tsOnly)
        tsOnly = dataManager.load(SoftDeleteEntity).id(tsOnly.getId()).softDeletion(false).one()
        Date afterDelete = timeSource.currentTimestamp()


        then:
        ((EntityEntrySoftDelete) entity.__getEntityEntry()).isDeleted()
        entity.whoDeleted.equals("admin")
        beforeOrEquals(beforeDelete, entity.timeOfDeletion)
        afterOrEquals(afterDelete, entity.timeOfDeletion)

        ((EntityEntrySoftDelete) tsOnly.__getEntityEntry()).isDeleted()
        ((EntityEntrySoftDelete) tsOnly.__getEntityEntry()).getDeletedBy() == null
        beforeOrEquals(beforeDelete, tsOnly.timeOfDeletion)
        afterOrEquals(afterDelete, tsOnly.timeOfDeletion)
        !dataManager.load(tsOnly.class).id(tsOnly.id).optional().isPresent()
        dataManager.load(tsOnly.class).id(tsOnly.id).softDeletion(false).optional().isPresent()


        cleanup:
        authenticator.end()
    }

    def "Soft deletion for collection"() {
        setup:
        authenticator.begin("admin")

        EntityWithSoftDeletedCollection parent = dataManager.save(dataManager.create(EntityWithSoftDeletedCollection))

        SoftDeleteEntity el1 = dataManager.create(SoftDeleteEntity)
        el1.setParent(parent)
        el1 = dataManager.save(el1)

        SoftDeleteEntity el2 = dataManager.create(SoftDeleteEntity)
        el2.setParent(parent)
        el2 = dataManager.save(el2)

        when:
        parent = dataManager.load(EntityWithSoftDeletedCollection).id(parent.getId()).fetchPlanProperties("collection").one()

        then:
        parent.collection.size() == 2

        when:
        dataManager.remove(el1)
        parent = dataManager.load(EntityWithSoftDeletedCollection).id(parent.getId()).fetchPlanProperties("collection").one()

        then:
        parent.collection.size() == 1
        parent.collection.get(0).id == el2.id

        cleanup:
        authenticator.end()
    }

    def "Soft deletion for many to many collection"() {
        setup:
        authenticator.begin("admin")

        SoftDeleteEntity el1 = dataManager.create(SoftDeleteEntity)
        el1.title = "el1"
        el1 = dataManager.save(el1)

        EntityWithSoftDeletedCollection parentEntity = dataManager.save(dataManager.create(EntityWithSoftDeletedCollection))

        SoftDeleteEntity el2 = dataManager.create(SoftDeleteEntity)
        el2.title = "el2"
        el2.parent = parentEntity
        el2 = dataManager.save(el2)

        EntityWithSoftDeletedManyToManyCollection parent = dataManager.create(EntityWithSoftDeletedManyToManyCollection)
        parent.setCollection(new HashSet<SoftDeleteEntity>())
        parent.getCollection().add(el1)
        parent.getCollection().add(el2)
        parent = dataManager.save(parent)

        when:
        parent = dataManager.load(EntityWithSoftDeletedManyToManyCollection).id(parent.getId()).fetchPlanProperties("collection").one()

        then:
        parent.collection.size() == 2

        when:
        dataManager.remove(el1)
        parent = dataManager.load(EntityWithSoftDeletedManyToManyCollection).id(parent.getId()).fetchPlanProperties("collection").one()

        then:
        parent.collection.size() == 1
        parent.collection.iterator().next().id == el2.id

        cleanup:
        authenticator.end()
    }

    def "Restore soft deleted entity"() {
        setup:
        authenticator.begin("admin")

        SoftDeleteEntity softDeleteEntity = dataManager.create(SoftDeleteEntity)
        softDeleteEntity.title = "el1"
        softDeleteEntity = dataManager.save(softDeleteEntity)

        def softDelID = softDeleteEntity.id

        def parent = dataManager.create(EntityWithSoftDeletedRef)
        parent.softDeleteEntity = softDeleteEntity
        parent = dataManager.save(parent)

        when:
        parent = dataManager.load(EntityWithSoftDeletedRef)
                .id(parent.id)
                .fetchPlanProperties("softDeleteEntity")
                .one()

        then:
        parent.softDeleteEntity == softDeleteEntity

        when:
        softDeleteEntity = dataManager.load(SoftDeleteEntity)
                .id(softDeleteEntity.id)
                .one()
        dataManager.remove(softDeleteEntity)
        parent = dataManager.load(EntityWithSoftDeletedRef)
                .id(parent.id)
                .fetchPlanProperties("softDeleteEntity")
                .one()

        then:
        parent.softDeleteEntity == null

        when:
        parent = dataManager.save(parent)
        parent = dataManager.load(EntityWithSoftDeletedRef)
                .id(parent.id)
                .fetchPlanProperties("softDeleteEntity")
                .one()

        then:
        parent.softDeleteEntity == null

        when:
        jdbcTemplate.update("update TEST_HARDDELETE_ENTITY set TIME_OF_DELETION = null where id = ?",
                JmixUUIDTypeDescriptor.ToStringTransformer.INSTANCE.transform(softDelID))
        softDeleteEntity = dataManager.load(SoftDeleteEntity)
                .id(softDelID)
                .one()
        parent = dataManager.load(EntityWithSoftDeletedRef)
                .id(parent.id)
                .fetchPlanProperties("softDeleteEntity")
                .one()

        then:
        softDeleteEntity != null
        parent.softDeleteEntity == softDeleteEntity

        cleanup:
        authenticator.end()
    }

    static boolean beforeOrEquals(Date first, Date second) {
        return first.before(second) || first.equals(second)
    }

    static boolean afterOrEquals(Date first, Date second) {
        return first.after(second) || first.equals(second)
    }

}
