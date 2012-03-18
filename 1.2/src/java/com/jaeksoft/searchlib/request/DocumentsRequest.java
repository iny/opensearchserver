/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.request;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.util.External;

public class DocumentsRequest implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2369658807248539632L;

	private DocumentRequest[] requestedDocuments;

	private FieldList<SnippetField> snippetFieldList;

	private FieldList<Field> returnFieldList;

	private transient FieldList<Field> documentFieldList;

	public DocumentsRequest() {
		documentFieldList = null;
	}

	private DocumentsRequest(SearchRequest searchRequest)
			throws ParseException, SyntaxError, IOException, SearchLibException {
		requestedDocuments = null;
		this.snippetFieldList = searchRequest.getSnippetFieldList();
		for (SnippetField snippetField : snippetFieldList)
			snippetField.initSearchTerms(searchRequest);
		this.returnFieldList = searchRequest.getReturnFieldList();
	}

	/**
	 * Build a new DocumentsRequest by extracting requested documents (from
	 * request.start to request.end)
	 * 
	 * @param resultSingle
	 * @throws ParseException
	 * @throws SyntaxError
	 * @throws IOException
	 * @throws SearchLibException
	 */
	public DocumentsRequest(Result result) throws ParseException, SyntaxError,
			IOException, SearchLibException {
		this(result.getSearchRequest());
		int start = result.getSearchRequest().getStart();
		int rows = result.getDocumentCount();
		if (rows <= 0)
			return;
		requestedDocuments = new DocumentRequest[rows];
		ResultScoreDoc[] docs = result.getDocs();
		for (int i = 0; i < rows; i++) {
			ResultScoreDoc doc = docs[i + start];
			requestedDocuments[i] = new DocumentRequest(doc, i);
		}
	}

	/**
	 * Build a new DocumentsRequest with only documents that match the indexName
	 * 
	 * @param resultGroup
	 * @param indexName
	 * @throws ParseException
	 * @throws SyntaxError
	 * @throws IOException
	 */
	public DocumentsRequest(DocumentsRequest documentsRequest)
			throws ParseException, SyntaxError, IOException {

		this.snippetFieldList = documentsRequest.snippetFieldList;
		this.returnFieldList = documentsRequest.returnFieldList;
		this.documentFieldList = documentsRequest.documentFieldList;

		DocumentRequest[] tempDocs = new DocumentRequest[documentsRequest.requestedDocuments.length];
		int l = 0;
		for (DocumentRequest doc : documentsRequest.requestedDocuments)
			tempDocs[l++] = doc;
		requestedDocuments = new DocumentRequest[l];
		l = 0;
		for (DocumentRequest doc : tempDocs) {
			if (doc == null)
				break;
			requestedDocuments[l++] = doc;
		}
	}

	public DocumentRequest[] getRequestedDocuments() {
		return requestedDocuments;
	}

	public FieldList<SnippetField> getSnippetFieldList() {
		return snippetFieldList;
	}

	public FieldList<Field> getReturnFieldList() {
		return returnFieldList;
	}

	public FieldList<Field> getDocumentFieldList() {
		if (documentFieldList != null)
			return documentFieldList;
		documentFieldList = new FieldList<Field>(returnFieldList);
		Iterator<SnippetField> it = snippetFieldList.iterator();
		while (it.hasNext())
			documentFieldList.add(new Field(it.next()));
		return documentFieldList;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		int l = in.readInt();
		if (l > 0) {
			requestedDocuments = new DocumentRequest[l];
			External.readObjectArray(in, requestedDocuments);
		}
		snippetFieldList = (FieldList<SnippetField>) in.readObject();
		returnFieldList = (FieldList<Field>) in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeObjectArray(requestedDocuments, out);
		out.writeObject(snippetFieldList);
		out.writeObject(returnFieldList);
	}

	public boolean isEmpty() {
		if (requestedDocuments == null)
			return true;
		return requestedDocuments.length == 0;
	}

}