// Copyright (C) 2011 Splunk Inc.
//
// Splunk Inc. licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.splunk.shuttl.archiver.bucketsize;

import java.io.File;

import com.splunk.shuttl.archiver.archive.ArchiveConfiguration;
import com.splunk.shuttl.archiver.archive.PathResolver;
import com.splunk.shuttl.archiver.filesystem.ArchiveFileSystem;
import com.splunk.shuttl.archiver.filesystem.ArchiveFileSystemFactory;
import com.splunk.shuttl.archiver.filesystem.transaction.Transaction;
import com.splunk.shuttl.archiver.filesystem.transaction.TransactionProvider;
import com.splunk.shuttl.archiver.model.Bucket;

/**
 * Gets a {@link Bucket}'s size. Both a remote bucket and a local bucket. <br/>
 * <br/>
 * This is needed because we want to know how big the {@link Bucket} will be on
 * the local file system and there's no guarantee that the size on the archive
 * file system is the same as on local disk. We therefore need to be able to put
 * and get the local file system size of the bucket, from the archive file
 * system.
 */
public class ArchiveBucketSize {

	private final PathResolver pathResolver;
	private final BucketSizeIO bucketSizeIO;
	private final ArchiveFileSystem archiveFileSystem;

	/**
	 * @see ArchiveBucketSize
	 */
	public ArchiveBucketSize(PathResolver pathResolver,
			BucketSizeIO bucketSizeIO, ArchiveFileSystem archiveFileSystem) {
		this.pathResolver = pathResolver;
		this.bucketSizeIO = bucketSizeIO;
		this.archiveFileSystem = archiveFileSystem;
	}

	/**
	 * @return size of an archived bucket on the local file system.
	 */
	public long getSize(Bucket bucket) {
		String filePathForSizeFile = pathResolver
				.getBucketSizeFileUriForBucket(bucket);
		return bucketSizeIO.readSizeFromRemoteFile(filePathForSizeFile);
	}

	/**
	 * @return a transaction for putting bucket size on the archiveFileSystem.
	 */
	public Transaction getBucketSizeTransaction(Bucket bucket) {
		File fileWithBucketSize = bucketSizeIO.getFileWithBucketSize(bucket);
		String temp = pathResolver.resolveTempPathForBucketSize(bucket);
		String bucketSizeFilePath = pathResolver
				.getBucketSizeFileUriForBucket(bucket);
		return TransactionProvider.createPut(archiveFileSystem,
				fileWithBucketSize.getAbsolutePath(), temp, bucketSizeFilePath);
	}

	/**
	 * Instance with path resolver and archive file system.
	 */
	public static ArchiveBucketSize create(PathResolver pathResolver,
			ArchiveFileSystem archiveFileSystem, BucketSizeIO bucketSizeIO) {
		return new ArchiveBucketSize(pathResolver, bucketSizeIO, archiveFileSystem);
	}

	/**
	 * Instance from config.
	 */
	public static ArchiveBucketSize create(ArchiveConfiguration config,
			BucketSizeIO bucketSizeIO) {
		return create(new PathResolver(config),
				ArchiveFileSystemFactory.getWithConfiguration(config), bucketSizeIO);
	}
}
