/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.lsp4e.operations.symbols;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.ui.quickaccess.IQuickAccessComputer;
import org.eclipse.ui.quickaccess.QuickAccessElement;

public class WorkspaceSymbolsQuickAccessProvider implements IQuickAccessComputer {


	private List<@NonNull LanguageServer> usedLanguageServers;

	@Override
	public QuickAccessElement[] computeElements() {
		this.usedLanguageServers = LanguageServiceAccessor.getActiveLanguageServers(serverCapabilities -> Boolean.TRUE.equals(serverCapabilities.getWorkspaceSymbolProvider()));
		if (usedLanguageServers.isEmpty()) {
			return new QuickAccessElement[0];
		}
		WorkspaceSymbolParams params = new WorkspaceSymbolParams(""); // see https://github.com/Microsoft/language-server-protocol/issues/740 //$NON-NLS-1$
		List<QuickAccessElement> res = Collections.synchronizedList(new ArrayList<>());
		CompletableFuture.allOf(usedLanguageServers.stream().map(ls ->
			ls.getWorkspaceService().symbol(params).thenAcceptAsync(symbols ->
				res.addAll(symbols.stream().map(WorkspaceSymbolQuickAccessElement::new).collect(Collectors.toList()))
		)).toArray(CompletableFuture[]::new)).join();
		return res.toArray(new QuickAccessElement[res.size()]);
	}

	@Override
	public void resetState() {
	}

	@Override
	public boolean needsRefresh() {
		return this.usedLanguageServers == null || !this.usedLanguageServers.equals(LanguageServiceAccessor.getActiveLanguageServers(serverCapabilities -> Boolean.TRUE.equals(serverCapabilities.getWorkspaceSymbolProvider())));
	}

}
