/*
 * jFCPlib - FileEntry.java - Copyright © 2008–2016 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.fcp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static net.pterodactylus.fcp.UploadFrom.direct;
import static net.pterodactylus.fcp.UploadFrom.disk;
import static net.pterodactylus.fcp.UploadFrom.redirect;

/**
 * Container class for file entry data.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 * @see ClientPutComplexDir#addFileEntry(FileEntry)
 */
public class FileEntry {

	/**
	 * Creates a new file entry for a file that should be transmitted to the
	 * node in the payload of the message.
	 *
	 * @param name            The name of the file
	 * @param contentType     The content type of the file, or <code>null</code> to let the
	 *                        node auto-detect it
	 * @param length          The length of the file
	 * @param dataInputStream The input stream of the file
	 * @return A file entry
	 */
	public static FileEntry createDirectFileEntry(String name, String contentType, long length, InputStream dataInputStream) {
		FileEntry directFileEntry = new FileEntry(name, direct) {
			@Override
			public InputStream getInputStream() {
				return dataInputStream;
			}
		};
		directFileEntry.fields.put("DataLength", String.valueOf(length));
		if (contentType != null) {
			directFileEntry.fields.put("Metadata.ContentType", contentType);
		}
		return directFileEntry;
	}

	/**
	 * Creates a new file entry for a file that should be uploaded from disk.
	 *
	 * @param name        The name of the file
	 * @param filename    The name of the file on disk
	 * @param contentType The content type of the file, or <code>null</code> to let the
	 *                    node auto-detect it
	 * @return A file entry
	 */
	public static FileEntry createDiskFileEntry(String name, String filename, String contentType) {
		FileEntry fileEntry = new FileEntry(name, disk);
		fileEntry.fields.put("Filename", filename);
		if (contentType != null) {
			fileEntry.fields.put("Metadata.ContentType", contentType);
		}
		return fileEntry;
	}

	/**
	 * Creates a new file entry for a file that redirects to another URI.
	 *
	 * @param name      The name of the file
	 * @param targetURI The target URI of the redirect
	 * @return A file entry
	 */
	public static FileEntry createRedirectFileEntry(String name, String targetURI) {
		FileEntry fileEntry = new FileEntry(name, redirect);
		fileEntry.fields.put("TargetURI", targetURI);
		return fileEntry;
	}

	public Map<String, String> getFields() {
		return fields;
	}

	/**
	 * Returns an {@link InputStream} delivering the content of this file. If
	 * this file entry is not of type {@link UploadFrom#direct}, the input
	 * stream returned by this method is empty.
	 *
	 * @return An {@link InputStream} delivering the content of a
	 * {@link UploadFrom#direct} file entry, or an empty input stream
	 */
	public InputStream getInputStream() {
		return new ByteArrayInputStream(new byte[0]);
	}

	/**
	 * Creates a new file entry with the given name and upload source.
	 *
	 * @param name       The name of the file
	 * @param uploadFrom The upload source of the file
	 */
	private FileEntry(String name, UploadFrom uploadFrom) {
		fields.put("Name", name);
		fields.put("UploadFrom", String.valueOf(uploadFrom));
	}

	private final Map<String, String> fields = new HashMap<>();

}
