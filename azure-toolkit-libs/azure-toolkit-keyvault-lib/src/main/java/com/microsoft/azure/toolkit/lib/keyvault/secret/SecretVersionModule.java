/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.keyvault.secret;

import com.azure.core.util.paging.ContinuablePage;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResourceModule;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.model.page.ItemPage;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource.isHttpException;
import static com.microsoft.azure.toolkit.lib.keyvault.KeyVault.getAccessPolicyConfiureAction;
import static com.microsoft.azure.toolkit.lib.keyvault.KeyVault.getAccessPolicyLearnMoreAction;

public class SecretVersionModule extends AbstractAzResourceModule<SecretVersion, Secret, SecretProperties> {
    public static final String NAME = "versions";

    public SecretVersionModule(Secret parent) {
        super(NAME, parent);
    }

    @Nonnull
    @Override
    protected Iterator<? extends ContinuablePage<String, SecretProperties>> loadResourcePagesFromAzure() {
        try {
            return Optional.ofNullable(getClient())
                .map(keys -> keys.listPropertiesOfSecretVersions(getParent().getName()).collectList().block())
                .map(ItemPage::new)
                .map(IteratorUtils::singletonIterator)
                .orElseGet(IteratorUtils::emptyIterator);
        } catch (final Throwable t) {
            if (isHttpException(t, 403)) {
                final Action<String> configure = getAccessPolicyConfiureAction(getParent().getKeyVault());
                final Action<String> learnMore = getAccessPolicyLearnMoreAction();
                throw new AzureToolkitRuntimeException(ExceptionUtils.getRootCauseMessage(t), configure, learnMore);
            }
            throw t;
        }
    }

    @Nullable
    @Override
    @AzureOperation(name = "azure/keyvault.load_secret_version.version", params = {"name"})
    protected SecretProperties loadResourceFromAzure(@Nonnull String name, @Nullable String resourceGroup) {
        try {
            final List<SecretProperties> versions = Optional.ofNullable(getClient())
                .map(keys -> keys.listPropertiesOfSecretVersions(getParent().getName()).collectList().block())
                .orElse(null);
            return Objects.isNull(versions) ? null : versions.stream()
                .filter(s -> StringUtils.equalsIgnoreCase(s.getVersion(), name))
                .findFirst()
                .orElse(null);
        } catch (final Throwable t) {
            if (isHttpException(t, 403)) {
                final Action<String> configure = getAccessPolicyConfiureAction(getParent().getKeyVault());
                final Action<String> learnMore = getAccessPolicyLearnMoreAction();
                throw new AzureToolkitRuntimeException(ExceptionUtils.getRootCauseMessage(t), configure, learnMore);
            }
            throw t;
        }
    }

    @Nonnull
    @Override
    protected SecretVersion newResource(@Nonnull SecretProperties remote) {
        return new SecretVersion(remote, this);
    }

    @Nonnull
    @Override
    protected SecretVersion newResource(@Nonnull String name, @Nullable String resourceGroupName) {
        return new SecretVersion(name, Objects.requireNonNull(resourceGroupName), this);
    }

    @Nonnull
    @Override
    protected AzResource.Draft<SecretVersion, SecretProperties> newDraftForUpdate(@Nonnull SecretVersion version) {
        return new SecretVersionDraft(version);
    }

    @Nullable
    @Override
    protected SecretAsyncClient getClient() {
        return getParent().getParent().getSecretClient();
    }

    @Nonnull
    @Override
    public String getResourceTypeName() {
        return "Secret Version";
    }
}

