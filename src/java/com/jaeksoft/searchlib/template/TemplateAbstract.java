/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.template;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.jaeksoft.searchlib.SearchLibException;

public abstract class TemplateAbstract {

	private final String rootPath;

	private final String[] resources;

	private final String publicName;

	private final String description;

	protected TemplateAbstract(String rootPath, String[] resources,
			String publicName, String description) {
		this.rootPath = rootPath;
		this.resources = resources;
		this.publicName = publicName;
		this.description = description;
	}

	public String getPublicName() {
		return publicName;
	}

	public String getDescription() {
		return description;
	}

	public void createIndex(File indexDir) throws SearchLibException,
			IOException {

		if (!indexDir.mkdir())
			throw new SearchLibException("directory creation failed ("
					+ indexDir + ")");

		InputStream is = null;
		FileWriter target = null;
		for (String resource : resources) {

			String res = rootPath + "/" + resource;
			is = getClass().getResourceAsStream(res);
			if (is == null)
				throw new SearchLibException("Unable to find resource " + res);
			try {
				target = new FileWriter(new File(indexDir, resource));
				IOUtils.copy(is, target);
			} finally {
				if (target != null)
					target.close();
				if (is != null)
					is.close();
			}
		}
	}
}
