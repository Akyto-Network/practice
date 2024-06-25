package kezukdev.akyto.database;

import kezukdev.akyto.Practice;

public class DatabaseSetup {
	
	private final Practice main;
	
	public DatabaseSetup(final Practice main) {
		this.main = main;
		this.main.getFileSetup().loadEditor();
	}
    
}
