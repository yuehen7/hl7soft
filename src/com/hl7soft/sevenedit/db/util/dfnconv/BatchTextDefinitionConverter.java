package com.hl7soft.sevenedit.db.util.dfnconv;

import com.hl7soft.sevenedit.db.defs.MessageDefinition;
import com.hl7soft.sevenedit.db.defs.io.xml.XmlDefinitionWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class BatchTextDefinitionConverter {
	public static void main(String[] args) {
		BatchTextDefinitionConverter app = new BatchTextDefinitionConverter();
		app.run();
	}

	public void run() {
		try {
			Map messageDescriptions = readMessageDescriptions();

			File dir = new File("msg-defs-txt");
			File[] files = dir.listFiles();

			File outDir = new File("msg-defs-xml");
			outDir.mkdir();

			for (int i = 0; i < files.length; i++) {
				File file = files[i];

				System.out.println("Processing file: " + file.getName());

				String messageName = file.getName();
				messageName = messageName.substring(0, messageName.indexOf(46));
				messageName = messageName.toUpperCase();

				String messageDescription = (String) messageDescriptions.get(messageName);
				if (messageDescription == null) {
					throw new RuntimeException("Message description not found: " + messageName);
				}

				String data = readData(file);
				MessageDefinition definition = new TextDefinitionParser().parseMessageDefinition(data, messageName,
						null);
				definition.setDescription(messageDescription);

				FileOutputStream os = new FileOutputStream(
						new File(outDir.getAbsolutePath() + "/" + messageName.toLowerCase() + ".xml"));

				XmlDefinitionWriter.write(definition, os);
				os.flush();
				os.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<String, String> readMessageDescriptions() throws Exception {
		Map messageDescriptions = new HashMap();

		FileInputStream is = new FileInputStream("message_names2.csv");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = reader.readLine()) != null) {
			line = line.trim();

			if (line.length() != 0) {
				int idx = line.indexOf(59);
				String messageName = line.substring(0, idx).trim().toUpperCase();
				String messageDescription = line.substring(idx + 1).trim();

				messageDescriptions.put(messageName, messageDescription);
			}
		}
		return messageDescriptions;
	}

	private String readData(File file) {
		try {
			FileInputStream is = new FileInputStream(file);
			byte[] buf = new byte[is.available()];
			is.read(buf);
			is.close();

			return new String(buf);
		} catch (Exception e) {
			throw new RuntimeException("Error reading data from file.", e);
		}
	}
}