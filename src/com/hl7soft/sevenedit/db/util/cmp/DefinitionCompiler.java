package com.hl7soft.sevenedit.db.util.cmp;

import com.hl7soft.sevenedit.db.defs.DefinitionFactory;
import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.db.defs.MessageDefinition;
import com.hl7soft.sevenedit.db.defs.SegmentDefinition;
import com.hl7soft.sevenedit.db.defs.io.bin.DefinitionFormatReader;
import com.hl7soft.sevenedit.db.defs.io.bin.DefinitionFormatWriter;
import com.hl7soft.sevenedit.db.defs.io.xml.XmlDefinitionReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

public class DefinitionCompiler {
	public static void main(String[] args) {
		DefinitionCompiler app = new DefinitionCompiler();
		app.run(args);
	}

	public void run(String[] args) {
		try {
			if ((args == null) || (args.length == 0)) {
				printTips();
				return;
			}

			Arguments arguments = new Arguments(args);

			String dbFolder = arguments.getDbFolder();
			if (dbFolder == null) {
				printTips();
				return;
			}

			String outFileName = arguments.getOutFileName();
			if (outFileName == null) {
				outFileName = dbFolder + ".dfn";
			}

			File dbFolderDir = new File(dbFolder);
			if (!dbFolderDir.exists()) {
				throw new RuntimeException("Database folder not found: " + dbFolderDir.getAbsolutePath());
			}

			DefinitionFactory definitionFactory = new DefinitionFactory();

			File[] subFolders = dbFolderDir.listFiles();
			for (int i = 0; i < subFolders.length; i++) {
				File subFolder = subFolders[i];

				if (subFolder.isDirectory()) {
					File descriptorFile = new File(subFolder.getAbsolutePath() + "/descriptor");
					if ((descriptorFile.exists()) && (descriptorFile.isFile())) {
						Properties props = new Properties();
						FileInputStream is = new FileInputStream(descriptorFile);
						props.load(is);
						is.close();

						String version = props.getProperty("version");

						System.out.println("Compiling version: " + version);

						File messagesFolder = new File(subFolder.getAbsolutePath() + "/messages");
						if ((messagesFolder.exists()) && (messagesFolder.isDirectory())) {
							File[] messageFiles = messagesFolder.listFiles();
							for (int j = 0; j < messageFiles.length; j++) {
								File messageFile = messageFiles[j];

								if (messageFile.isFile()) {
									is = new FileInputStream(messageFile);
									MessageDefinition dfn = XmlDefinitionReader.readMessageDefinition(is);
									dfn.setVersion(version);
									is.close();

									definitionFactory.addMessageDefinition(dfn);
								}
							}

							File segmentsFolder = new File(subFolder.getAbsolutePath() + "/segments");
							if ((segmentsFolder.exists()) && (segmentsFolder.isDirectory())) {
								File[] segmentFiles = segmentsFolder.listFiles();
								for (int j = 0; j < segmentFiles.length; j++) {
									File segmentFile = segmentFiles[j];

									if (segmentFile.isFile()) {
										is = new FileInputStream(segmentFile);
										SegmentDefinition segmentDefinition = XmlDefinitionReader
												.readSegmentDefinition(is);
										segmentDefinition.setVersion(version);
										is.close();

										definitionFactory.addSegmentDefinition(segmentDefinition);
									}
								}

								File fieldsFolder = new File(subFolder.getAbsolutePath() + "/datatypes");
								if ((fieldsFolder.exists()) && (fieldsFolder.isDirectory())) {
									File[] fieldFiles = fieldsFolder.listFiles();
									for (int j = 0; j < fieldFiles.length; j++) {
										File fieldFile = fieldFiles[j];

										if (fieldFile.isFile()) {
											is = new FileInputStream(fieldFile);
											FieldDefinition fieldDefinition = XmlDefinitionReader
													.readFieldDefinition(is);
											fieldDefinition.setVersion(version);
											is.close();

											definitionFactory.addFieldDefinition(fieldDefinition);
										}
									}
								}
							}
						}
					}
				}
			}
			FileOutputStream os = new FileOutputStream(new File(outFileName));
			new DefinitionFormatWriter().write(definitionFactory, os);
			os.close();

			DefinitionFormatReader reader = new DefinitionFormatReader(new File(outFileName));
			reader.read();
			DefinitionFactory definitionFactory2 = reader.getDefinitionFactory();

			System.out.println("Verifying...");

			compare(definitionFactory2, definitionFactory);

			System.out.println("DONE");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println();
			System.out.println("ERROR: " + e.getMessage());
		}
	}

	private void compare(DefinitionFactory factory, DefinitionFactory referenceFactory) {
		try {
			List referenceVersions = referenceFactory.getVersions();
			List versions = factory.getVersions();

			if (((referenceVersions == null) && (versions != null))
					|| ((referenceVersions != null) && (versions == null))) {
				throw new RuntimeException();
			}

			versions.removeAll(referenceVersions);
			if (versions.size() != 0) {
				throw new RuntimeException();
			}

			int i = 0;
			for (int n = referenceVersions.size(); i < n; i++) {
				String version = (String) referenceVersions.get(i);

				List referenceMessageNames = referenceFactory.getMessages(version);
				List messageNames = factory.getMessages(version);

				messageNames.removeAll(referenceMessageNames);
				if (messageNames.size() != 0) {
					throw new RuntimeException();
				}

				List referenceSegmentNames = referenceFactory.getSegments(version);
				List segmentNames = factory.getSegments(version);

				segmentNames.removeAll(referenceSegmentNames);
				if (segmentNames.size() != 0) {
					throw new RuntimeException();
				}

				List referenceFieldNames = referenceFactory.getFields(version);
				List fieldNames = factory.getFields(version);

				fieldNames.removeAll(referenceFieldNames);
				if (fieldNames.size() != 0)
					throw new RuntimeException();
			}
		} catch (Exception e) {
			throw new RuntimeException("Verification error.", e);
		}
	}

	private void printTips() {
		System.out.println("Usage: defcomp.sh -dbfolder database_dir -outfile model.dfn");
	}

	class Arguments {
		String dbFolder;
		String outFileName;
		String[] args;

		public Arguments(String[] args) {
			this.args = args;
			init();
		}

		private void init() {
			if (this.args.length % 2 != 0) {
				throw new RuntimeException("Bad arguments count.");
			}
			try {
				for (int i = 0; i < this.args.length; i++)
					if ("-outfile".equalsIgnoreCase(this.args[i]))
						this.outFileName = this.args[(i + 1)];
					else if ("-dbfolder".equalsIgnoreCase(this.args[i]))
						this.dbFolder = this.args[(i + 1)];
			} catch (Exception e) {
				throw new RuntimeException("Invalid arguments.");
			}
		}

		public String getDbFolder() {
			return this.dbFolder;
		}

		public String getOutFileName() {
			return this.outFileName;
		}
	}
}