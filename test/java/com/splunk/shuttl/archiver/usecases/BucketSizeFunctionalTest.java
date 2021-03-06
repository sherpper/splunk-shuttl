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
package com.splunk.shuttl.archiver.usecases;

import static com.splunk.shuttl.testutil.TUtilsFile.*;
import static org.testng.AssertJUnit.*;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.splunk.shuttl.archiver.LocalFileSystemPaths;
import com.splunk.shuttl.archiver.archive.ArchiveConfiguration;
import com.splunk.shuttl.archiver.archive.BucketArchiver;
import com.splunk.shuttl.archiver.archive.BucketShuttlerFactory;
import com.splunk.shuttl.archiver.filesystem.ArchiveFileSystem;
import com.splunk.shuttl.archiver.filesystem.ArchiveFileSystemFactory;
import com.splunk.shuttl.archiver.filesystem.PathResolver;
import com.splunk.shuttl.archiver.listers.ListsBucketsFiltered;
import com.splunk.shuttl.archiver.listers.ListsBucketsFilteredFactory;
import com.splunk.shuttl.archiver.metastore.ArchiveBucketSize;
import com.splunk.shuttl.archiver.model.Bucket;
import com.splunk.shuttl.archiver.model.IllegalIndexException;
import com.splunk.shuttl.archiver.model.LocalBucket;
import com.splunk.shuttl.archiver.thaw.BucketThawer;
import com.splunk.shuttl.archiver.thaw.BucketThawerFactory;
import com.splunk.shuttl.archiver.thaw.SplunkIndexesLayer;
import com.splunk.shuttl.archiver.usecases.util.FakeSplunkIndexesLayer;
import com.splunk.shuttl.testutil.TUtilsBucket;
import com.splunk.shuttl.testutil.TUtilsFunctional;

@Test(groups = { "functional" })
public class BucketSizeFunctionalTest {

	private BucketArchiver bucketArchiver;
	private BucketThawer bucketThawer;
	private ArchiveConfiguration config;
	private File thawLocation;
	private ArchiveBucketSize archiveBucketSize;
	private File archiverData;

	@BeforeMethod
	public void setUp() throws IllegalIndexException {
		config = TUtilsFunctional.getLocalFileSystemConfiguration();
		archiverData = createDirectory();
		LocalFileSystemPaths localFileSystemPaths = new LocalFileSystemPaths(
				archiverData.getAbsolutePath());
		bucketArchiver = BucketShuttlerFactory.createWithConfAndLocalPaths(config,
				localFileSystemPaths);
		SplunkIndexesLayer SplunkIndexesLayer = new FakeSplunkIndexesLayer(
				thawLocation);
		thawLocation = createDirectory();

		bucketThawer = BucketThawerFactory
				.createWithConfigAndSplunkSettingsAndLocalFileSystemPaths(config,
						SplunkIndexesLayer, localFileSystemPaths);

		PathResolver pathResolver = new PathResolver(config);
		ArchiveFileSystem archiveFileSystem = ArchiveFileSystemFactory
				.getWithConfiguration(config);
		archiveBucketSize = ArchiveBucketSize.create(pathResolver,
				archiveFileSystem, localFileSystemPaths);
	}

	@AfterMethod
	public void tearDown() {
		TUtilsFunctional.tearDownLocalConfig(config);
		FileUtils.deleteQuietly(thawLocation);
		FileUtils.deleteQuietly(archiverData);
	}

	public void BucketSize_archiveBucket_remoteBucketHasSameSizeAsBeforeArchiving() {
		LocalBucket bucket = TUtilsBucket.createRealBucket();
		long bucketSize = bucket.getSize();

		TUtilsFunctional.archiveBucket(bucket, bucketArchiver);
		ListsBucketsFiltered listsBucketsFiltered = ListsBucketsFilteredFactory
				.create(config);
		List<Bucket> listBucketsInIndex = listsBucketsFiltered
				.listFilteredBucketsAtIndex(bucket.getIndex(), bucket.getEarliest(),
						bucket.getLatest());

		assertEquals(1, listBucketsInIndex.size());
		Bucket bucketInArchive = listBucketsInIndex.get(0);
		assertEquals(bucketSize,
				(long) archiveBucketSize.readBucketSize(bucketInArchive));
	}

	public void BucketSize_bucketRoundTrip_bucketGetSizeShouldBeTheSameBeforeArchiveAndAfterThaw() {
		LocalBucket bucket = TUtilsBucket.createRealBucket();

		TUtilsFunctional.archiveBucket(bucket, bucketArchiver);
		bucketThawer.thawBuckets(bucket.getIndex(), bucket.getEarliest(),
				bucket.getLatest());

		List<LocalBucket> thawedBuckets = bucketThawer.getThawedBuckets();
		assertEquals(1, thawedBuckets.size());
		Bucket thawedBucket = thawedBuckets.get(0);
		assertEquals(bucket.getSize(), thawedBucket.getSize());
	}
}
