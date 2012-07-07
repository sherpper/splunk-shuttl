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
package com.splunk.shuttl.archiver;

import static com.splunk.shuttl.testutil.TUtilsFile.*;
import static org.testng.Assert.*;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "fast-unit" })
public class LocalFileSystemConstantsTest {

	private LocalFileSystemConstants localFileSystemConstants;
	private String testDirectoryPath;

	@BeforeMethod
	public void setUp() {
		testDirectoryPath = createFilePath().getAbsolutePath();
		localFileSystemConstants = new LocalFileSystemConstants(testDirectoryPath);
		removeArchiverDirectory();
	}

	private void removeArchiverDirectory() {
		File directory = localFileSystemConstants.getArchiverDirectory();
		FileUtils.deleteQuietly(directory);
	}

	@AfterMethod
	public void tearDown() {
		removeArchiverDirectory();
	}

	@Test(groups = { "fast-unit" })
	public void getArchiverDirectory_givenTestDirectory_dirIsChildToTestDirectory() {
		assertDoesNotExist(testDirectoryPath);
		File dir = localFileSystemConstants.getArchiverDirectory();
		assertEquals(testDirectoryPath, dir.getParent());
	}

	private void assertDoesNotExist(String path) {
		assertFalse(new File(path).exists());
	}

	public void getSafeLocation_setUp_dirExistsInsideArchiverDirectory() {
		File safeDir = localFileSystemConstants.getSafeDirectory();
		assertExistsInsideArchiverDirectory(safeDir);
	}

	private void assertExistsInsideArchiverDirectory(File failLocation) {
		assertTrue(failLocation.exists());
		assertParentIsArchiverDirectory(failLocation);
	}

	private void assertParentIsArchiverDirectory(File safeDir) {
		assertEquals(localFileSystemConstants.getArchiverDirectory(),
				safeDir.getParentFile());
	}

	public void getFailLocation_setUp_dirExistsInsideArchiverDirectory() {
		File failLocation = localFileSystemConstants.getFailDirectory();
		assertExistsInsideArchiverDirectory(failLocation);
	}

	public void getArchiveLocksDirectory_setUp_dirExistsInsideArchiverDirectory() {
		assertExistsInsideArchiverDirectory(localFileSystemConstants
				.getArchiveLocksDirectory());
	}

	public void getCsvDirectory_setUp_dirExistsInsideArchiverDirectory() {
		assertExistsInsideArchiverDirectory(localFileSystemConstants
				.getCsvDirectory());
	}

	public void getThawLocksDirectory_setUp_dirExistsInsideArchiverDirectory() {
		assertExistsInsideArchiverDirectory(localFileSystemConstants
				.getThawLocksDirectory());
	}

	public void getThawTransferDirectory_setUp_dirExistsInsideArchiverDirectory() {
		assertExistsInsideArchiverDirectory(localFileSystemConstants
				.getThawTransfersDirectory());
	}

	public void getArchiverDirectory_givenTildeWithoutRoot_resolvesTildeAsUserHome() {
		File archiverDirectory = new LocalFileSystemConstants(
				"~/archiver_directory").getArchiverDirectory();
		File expected = new File(FileUtils.getUserDirectoryPath(),
				"archiver_directory");
		assertEquals(expected.getAbsolutePath(), archiverDirectory.getParentFile()
				.getAbsolutePath());
	}

	public void getArchiverDirectory_givenUriWithFileSchemeAndTilde_resolvesTildeAsUserHome() {
		File archiverDirectory = new LocalFileSystemConstants(
				"file:/~/archiver_dir").getArchiverDirectory();
		File expected = new File(FileUtils.getUserDirectoryPath(), "archiver_dir");
		assertEquals(expected.getAbsolutePath(), archiverDirectory.getParentFile()
				.getAbsolutePath());
	}

	public void create_staticWithNoArgs_getArchiverDirectoryIsInATemporaryDirectory() {
		File archiverDirectory = LocalFileSystemConstants.create()
				.getArchiverDirectory();
		assertTrue(archiverDirectory.getAbsolutePath().contains(
				FileUtils.getTempDirectoryPath()));
	}

	public void create_staticWithNoArgs_dirNameContainsTheWordForTests() {
		File archiverDirectory = LocalFileSystemConstants.create()
				.getArchiverDirectory();
		String absolutePath = archiverDirectory.getAbsolutePath();
		assertTrue(absolutePath.toLowerCase().contains("fortests"));
	}
}
