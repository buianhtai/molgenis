package org.molgenis.file.ingest.execution;

import org.molgenis.file.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;

/**
 * Downloads a file from a URL to the {@link FileStore}
 */
@Component
public class FileStoreDownloadImpl implements FileStoreDownload
{
	private final FileStore fileStore;

	@Autowired
	public FileStoreDownloadImpl(FileStore fileStore)
	{
		this.fileStore = fileStore;
	}

	@Override
	public File downloadFile(String url, String folderName, String fileName)
	{
		try
		{
			File folder = new File(fileStore.getStorageDir(), folderName);
			folder.mkdir();

			InputStream in = new URL(url).openStream();
			String filename = folderName + '/' + fileName;

			return fileStore.store(in, filename);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

}
