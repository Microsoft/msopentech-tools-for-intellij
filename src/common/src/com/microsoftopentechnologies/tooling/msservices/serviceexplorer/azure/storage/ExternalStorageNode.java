/**
 * Copyright 2014 Microsoft Open Technologies Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoftopentechnologies.tooling.msservices.serviceexplorer.azure.storage;

import com.microsoftopentechnologies.tooling.msservices.components.DefaultLoader;
import com.microsoftopentechnologies.tooling.msservices.helpers.ExternalStorageHelper;
import com.microsoftopentechnologies.tooling.msservices.helpers.NotNull;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoftopentechnologies.tooling.msservices.model.storage.ClientStorageAccount;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.EventHelper.EventStateHandle;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.Node;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.NodeAction;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.azure.AzureNodeActionPromptListener;

import java.util.Map;

public class ExternalStorageNode extends ClientStorageNode {
    private class DetachAction extends AzureNodeActionPromptListener {
        public DetachAction() {
            super(ExternalStorageNode.this,
                    String.format("This operation will detach external storage account %s.\nAre you sure you want to continue?", clientStorageAccount.getName()),
                    "Detaching External Storage Account");
        }

        @Override
        protected void azureNodeAction(NodeActionEvent e, @NotNull EventStateHandle stateHandle)
                throws AzureCmdException {
            Node node = e.getAction().getNode();
            node.getParent().removeDirectChildNode(node);

            ExternalStorageHelper.detach(clientStorageAccount);
        }

        @Override
        protected void onSubscriptionsChanged(NodeActionEvent e)
                throws AzureCmdException {
        }
    }

    private static final String WAIT_ICON_PATH = "externalstorageaccount.png";

    public ExternalStorageNode(StorageModule parent, ClientStorageAccount sm) {
        super(sm.getName(), sm.getName(), parent, WAIT_ICON_PATH, sm, true);

        loadActions();
    }

    @Override
    protected void refresh(@NotNull EventStateHandle eventState)
            throws AzureCmdException {
        removeAllChildNodes();

        if (clientStorageAccount.getPrimaryKey().isEmpty()) {
            try {
                NodeActionListener listener = DefaultLoader.getActions(this.getClass()).get(0).getConstructor().newInstance();
                listener.actionPerformedAsync(new NodeActionEvent(new NodeAction(this, this.getName()))).get();
            } catch (Throwable t) {
                throw new AzureCmdException("Error opening external storage", t);
            }
        } else {
            fillChildren(eventState);
        }
    }

    @Override
    protected Map<String, Class<? extends NodeActionListener>> initActions() {
        addAction("Detach", new DetachAction());

        return super.initActions();
    }
}