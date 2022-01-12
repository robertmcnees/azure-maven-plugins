/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.postgre;

import com.azure.resourcemanager.postgresql.models.Database;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.model.AzResourceModule;
import com.microsoft.azure.toolkit.lib.database.entity.IDatabase;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class PostgreSqlDatabase extends AbstractAzResource<PostgreSqlDatabase, PostgreSqlServer, Database> implements IDatabase {

    protected PostgreSqlDatabase(Database database, PostgreSqlDatabaseModule module) {
        this(database.name(), module.getParent().getResourceGroupName(), module);
    }

    protected PostgreSqlDatabase(@Nonnull String name, @Nonnull String resourceGroupName, @Nonnull PostgreSqlDatabaseModule module) {
        super(name, resourceGroupName, module);
    }

    @Override
    protected void refreshRemote() {
        this.remoteOptional().ifPresent(Database::refresh);
    }

    @Override
    public List<AzResourceModule<?, PostgreSqlDatabase, ?>> getSubModules() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public String loadStatus(@Nonnull Database remote) {
        return Status.UNKNOWN;
    }

    @Override
    public String getCollation() {
        return this.remoteOptional().map(Database::collation).orElse(null);
    }

    @Override
    public PostgreSqlServer getServer() {
        return this.getParent();
    }

    public String getCharset() {
        return this.remoteOptional().map(Database::charset).orElse(null);
    }
}
