// Copyright (C) 2011 Splunk Inc.
//
// Splunk Inc. licenses this file
// to you under the Apache License, Version 2.0 (the
// License); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an AS IS BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.splunk.shuttl.archiver.filesystem.hadoop;

import static com.splunk.shuttl.testutil.TUtilsFile.*;
import static org.testng.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileSystem;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.splunk.shuttl.testutil.TUtilsFile;
import com.splunk.shuttl.testutil.TUtilsFileSystem;

/**
 * Using the method naming convention:
 * [metodNamn]_[stateUnderTest]_[expectedOutcome]
 */
@Test(groups = { "fast-unit" })
public class HadoopArchiveFileSystemTest {

	private FileSystem fileSystem;
	private HadoopArchiveFileSystem hadoopArchiveFileSystem;

	@BeforeMethod
	public void beforeMethod() {
		fileSystem = TUtilsFileSystem.getLocalFileSystem();
		hadoopArchiveFileSystem = new HadoopArchiveFileSystem(fileSystem);
	}

	public void getFile_whenLocalFileAllreadyExist_localFileIsNotOverwritten()
			throws IOException, URISyntaxException {
	}

	public void listPath_listingAPathThatPointsToADirectory_aListThatContainsThePathsInsideSpecifiedDirectory()
			throws URISyntaxException, IOException {
		File dir = createDirectory();
		File file1 = createFileInParent(dir, "file1");
		File file2 = createFileInParent(dir, "file2");

		List<String> listing = hadoopArchiveFileSystem.listPath(dir
				.getAbsolutePath());

		assertEquals(2, listing.size());
		assertTrue(listing.contains(file1.getAbsolutePath()));
		assertTrue(listing.contains(file2.getAbsolutePath()));
	}

	public void listPath_listingAnEmptyDirectory_emptyList() throws IOException {
		File testDirectory = TUtilsFile.createDirectory();
		List<String> listing = hadoopArchiveFileSystem.listPath(testDirectory
				.getAbsolutePath());
		assertEquals(0, listing.size());
	}

	public void listPath_listingAPathThatPointsToAFile_aListOnlyContainingThePathToTheFile()
			throws IOException {
		File file = createFile();
		List<String> listing = hadoopArchiveFileSystem.listPath(file
				.getAbsolutePath());
		assertTrue(listing.contains(file.getAbsolutePath()));
	}

	public void listPath_listingAPathThatDoNotExist_emptyList()
			throws IOException, URISyntaxException {
		File file = createFilePath();
		assertFalse(file.exists());
		List<String> listing = hadoopArchiveFileSystem.listPath(file
				.getAbsolutePath());
		assertEquals(0, listing.size());
	}

	public void openFile_existingFileOnHadoop_inputStreamToFile()
			throws FileNotFoundException, IOException {
		File fileWithContent = createFileWithRandomContent();
		List<String> expectedContent = FileUtils.readLines(fileWithContent);

		InputStream openFile = hadoopArchiveFileSystem.openFile(fileWithContent
				.getAbsolutePath());
		assertEquals(expectedContent, IOUtils.readLines(openFile));
	}

	@Test(groups = { "fast-unit" })
	public void mkdirs_givenEmptyDirectory_canMakeDirectoryInTheEmptyOne()
			throws IOException {
		File emptyDir = createDirectory();
		assertTrue(TUtilsFile.isDirectoryEmpty(emptyDir));

		File nextLevelDir = new File(emptyDir, "next-level-dir");
		assertFalse(nextLevelDir.exists());
		hadoopArchiveFileSystem.mkdirs(nextLevelDir.getAbsolutePath());
		assertTrue(nextLevelDir.exists());
	}

	@Test(groups = { "fast-unit" })
	public void mkdirs_givenEmptyDir_canMakeDirsMultipleLevelsDown()
			throws IOException {
		File dir = createDirectory();
		File one = new File(dir, "one");
		File two = new File(one, "two");

		hadoopArchiveFileSystem.mkdirs(two.getAbsolutePath());
		assertTrue(two.exists());
	}

	@Test(groups = { "fast-unit" })
	public void mkdirs_givenExistingDir_doesNothing() throws IOException {
		hadoopArchiveFileSystem.mkdirs(createDirectory().getAbsolutePath());
	}

	@Test(groups = { "fast-unit" })
	public void rename_existingDir_renamesIt() throws IOException {
		File dir = createDirectory();
		File newName = new File(createDirectory(), "foo.bar");
		assertFalse(newName.exists());
		hadoopArchiveFileSystem.rename(dir.getAbsolutePath(),
				newName.getAbsolutePath());
		assertTrue(newName.exists());
		assertFalse(dir.exists());
	}
}
