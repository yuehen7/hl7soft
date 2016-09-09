package com.hl7soft.sevenedit.db.util.cmp;

import com.hl7soft.sevenedit.db.tables.io.bin.TableFormatWriter;
import com.hl7soft.sevenedit.db.tables.io.xml.XMLTableFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class TableCompiler {
	public static void main(String[] args) {
		TableCompiler app = new TableCompiler();
		app.run(args);
	}

	public void run(String[] args) {
		try {
			if ((args == null) || (args.length == 0)) {
				printTips();
				return;
			}

			Arguments arguments = new Arguments(args);

			String tableFileName = arguments.getTableFileName();
			if (tableFileName == null) {
				printTips();
				return;
			}

			String outFileName = arguments.getOutFileName();
			if (outFileName == null) {
				outFileName = tableFileName + ".tbl";
			}

			File inFile = new File(tableFileName);
			if ((!inFile.exists()) || (!inFile.isFile())) {
				throw new RuntimeException("Table file not found: " + inFile.getAbsolutePath());
			}

			System.out.println("Compiling tables...");

			FileInputStream is = new FileInputStream(inFile);
			XMLTableFactory xmlTableFactory = new XMLTableFactory(is);
			is.close();

			FileOutputStream os = new FileOutputStream(new File(outFileName));
			new TableFormatWriter().write(xmlTableFactory, os);
			os.close();

			System.out.println("DONE");
		} catch (Exception e) {
			System.out.println();
			System.out.println("ERROR: " + e.getMessage());
		}
	}

	private void printTips() {
		System.out.println("Usage: tblcomp.sh -tblfile tblfile.xml -outfile tables.tbl");
	}

	class Arguments {
		String tableFileName;
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
					else if ("-tblfile".equalsIgnoreCase(this.args[i]))
						this.tableFileName = this.args[(i + 1)];
			} catch (Exception e) {
				throw new RuntimeException("Invalid arguments.");
			}
		}

		public String getTableFileName() {
			return this.tableFileName;
		}

		public String getOutFileName() {
			return this.outFileName;
		}
	}
}