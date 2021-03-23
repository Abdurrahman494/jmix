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

package io.jmix.search.utils;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component("search_PropertyTools")
public class PropertyTools {

    private static final Logger log = LoggerFactory.getLogger(PropertyTools.class);

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Metadata metadata;

    /**
     * Finds properties of entity by provided path string. Path string supports wildcard '*'.
     * @param metaClass entity meta class
     * @param pathString path to property to find
     * @return map with effective property path as a key and property itself as a value
     */
    public Map<String, MetaPropertyPath> findPropertiesByPath(MetaClass metaClass, String pathString) {
        log.debug("Find properties by path: MetaClass={}, PathString={}", metaClass, pathString);
        if (hasWildcard(pathString)) {
            return findPropertiesByWildcardPath(metaClass, pathString);
        } else {
            MetaPropertyPath propertyPath = metaClass.getPropertyPath(pathString);
            if (propertyPath == null) {
                log.debug("Entity '{}' doesn't have property '{}'", metaClass, pathString);
                return Collections.emptyMap();
            } else {
                return Collections.singletonMap(pathString, propertyPath);
            }
        }
    }

    /**
     * Checks if entity primary key is used as index primary key.
     * @param metaClass entity meta class
     * @return true if entity pk property is used for indexing as identifier
     */
    public boolean isEntityIdUsedAsIndexPrimaryKey(MetaClass metaClass) {
        return getPrimaryKeyPropertyNameForIndexOpt(metaClass)
                .map(searchPk -> searchPk.equals(metadataTools.getPrimaryKeyName(metaClass)))
                .orElse(false);
    }

    /**
     * Gets name of property contains unique primary key is used as identifier for index object.
     * Throws exception if primary key can't be resolved.
     * @param metaClass entity meta class
     * @return property name
     */
    public String getPrimaryKeyPropertyNameForIndex(MetaClass metaClass) {
        return getPrimaryKeyPropertyNameForIndexOpt(metaClass).orElseThrow(
                () -> new RuntimeException("Proper primary key property not found for entity " + metaClass.getName())
        );
    }

    /**
     * Gets optional name of property contains unique primary key is used as identifier for index object.
     * @param metaClass entity meta class
     * @return Optional property name
     */
    public Optional<String> getPrimaryKeyPropertyNameForIndexOpt(MetaClass metaClass) {
        String primaryKeyPropertyName;
        if (metadataTools.hasCompositePrimaryKey(metaClass) && metadataTools.hasUuid(metaClass)) {
            primaryKeyPropertyName = metadataTools.getUuidPropertyName(metaClass.getJavaClass());
        } else {
            primaryKeyPropertyName = metadataTools.getPrimaryKeyName(metaClass);
        }
        return Optional.ofNullable(primaryKeyPropertyName);
    }

    /**
     * Extracts index primary key value from entity instance
     * @param instance entity instance
     * @return primary key value
     */
    public Optional<String> getPrimaryKeyForIndexOpt(Object instance) {
        MetaClass metaClass = metadata.getClass(instance);
        return getPrimaryKeyPropertyNameForIndexOpt(metaClass)
                .map(pk -> EntityValues.getValue(instance, pk))
                .map(Object::toString);
    }

    protected Map<String, MetaPropertyPath> findPropertiesByWildcardPath(MetaClass metaClass, String path) {
        if (StringUtils.isBlank(path)) {
            return Collections.emptyMap();
        }
        String[] pathItems = path.split("\\.");
        return findPropertiesByPathItems(metaClass, pathItems, new MetaPropertyPath(metaClass));
    }

    protected Map<String, MetaPropertyPath> findPropertiesByPathItems(MetaClass metaClass, String[] pathItems, MetaPropertyPath parentPath) {
        log.debug("Find properties by path items: entity={}, PathItems={}, parentPath={}", metaClass, Arrays.deepToString(pathItems), parentPath);
        if (pathItems.length == 0) {
            return Collections.emptyMap();
        }

        Map<String, MetaPropertyPath> result;

        String pathItem = pathItems[0];
        log.debug("Path Item = {}", pathItem);
        if (pathItems.length == 1) {
            log.debug("'{}' is the last level of path", pathItem);
            if (hasWildcard(pathItem)) {
                Pattern pattern = Pattern.compile(pathItem.replace("*", ".*")); //todo exclude inverse properties
                List<MetaProperty> localPropertiesByPattern = findLocalPropertiesByPattern(metaClass, pattern);
                result = localPropertiesByPattern.stream()
                        .filter(this::isSearchableProperty)
                        .map(property -> createPropertyPath(parentPath, property))
                        .collect(Collectors.toMap(MetaPropertyPath::toPathString, Function.identity()));
            } else {
                MetaProperty property = metaClass.findProperty(pathItem);
                if (property != null && isSearchableProperty(property)) {
                    result = new HashMap<>();
                    MetaPropertyPath newPath = createPropertyPath(parentPath, property);
                    result.put(newPath.toPathString(), newPath);
                } else {
                    result = Collections.emptyMap();
                }
            }
        } else {
            if (hasWildcard(pathItem)) {
                Pattern pattern = Pattern.compile(pathItem.replace("*", ".*"));
                List<MetaProperty> localPropertiesByPattern = findLocalPropertiesByPattern(metaClass, pattern);
                result = new HashMap<>();
                for (MetaProperty property : localPropertiesByPattern) {
                    if (isReferenceProperty(property)) {
                        MetaClass nextMetaClass = property.getRange().asClass();
                        MetaPropertyPath nextPath = createPropertyPath(parentPath, property);
                        result.putAll(findPropertiesByPathItems(nextMetaClass, Arrays.copyOfRange(pathItems, 1, pathItems.length), nextPath));
                    }
                }

            } else {
                MetaProperty property = metaClass.findProperty(pathItem);
                if (property != null && isReferenceProperty(property)) {
                    MetaClass nextMetaClass = property.getRange().asClass();
                    MetaPropertyPath nextPath = createPropertyPath(parentPath, property);
                    result = findPropertiesByPathItems(nextMetaClass, Arrays.copyOfRange(pathItems, 1, pathItems.length), nextPath);
                } else {
                    result = Collections.emptyMap();
                }
            }
        }
        return result;
    }

    protected MetaPropertyPath createPropertyPath(MetaPropertyPath parentPath, MetaProperty property) {
        return new MetaPropertyPath(parentPath, property);
    }

    protected List<MetaProperty> findLocalPropertiesByPattern(MetaClass metaClass, Pattern pattern) {
        Collection<MetaProperty> allLocalProperties = metaClass.getProperties();
        return allLocalProperties.stream()
                .filter((property) -> pattern.matcher(property.getName()).matches())
                .collect(Collectors.toList());
    }

    protected boolean isSearchableProperty(MetaProperty property) {
        return !metadataTools.isSystem(property);
    }

    protected boolean isReferenceProperty(MetaProperty metaProperty) {
        return metaProperty.getRange().isClass();
    }

    protected boolean hasWildcard(String path) {
        return path.contains("*");
    }
}
