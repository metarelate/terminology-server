/* 
 (C) British Crown Copyright 2011 - 2013, Met Office

 This file is part of terminology-server.

 terminology-server is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 terminology-server is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with terminology-server. If not, see <http://www.gnu.org/licenses/>.
*/

package net.metarelate.terminology.installer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

public class Installer {
	private static String packageDirectory=null;

	private static File sourceCommandDirFile=null;
	private static File sourceLibDirFile=null;
	private static File sourceWebDirFile=null;
	
	private static File sourceResourcesDirFile=null;
	private static File sourceConfigSubDirFile=null;
	private static File sourceAuthSubDirFile=null;
	private static File sourceTemplatesSubDirFile=null;
	
	private static File[] mustHaveConfTemplatesFiles=new File[InstallerConfig.mustHaveConfTemplates.length];
	
	private static String targetDir=null;
	private static String targetDBDir=null;
	private static String targetCacheDir=null;
	private static String targetGitDir=null;
	private static String targetConfDir=null;
	private static String targetAuthDir=null;
	private static String targetWebDir=null;
	private static String targetTemplatesDir=null;
	private static String targetLibDir=null;
	private static String targetCommandDir=null;
	
	private static String instanceIDString=null;
	private static String userID=null;
	private static String baseURL=null;
	private static String baseDisk=null;
	private static String pdfLatexFile=null;
	
	
	private static  BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	public static void main(String[] args) {
		System.out.println("Welcome to the Terminology Manager (Simple) Installer\n"); 
	    if(!verify()) {
	    		System.out.println ("I have found some problem with the current package.\n" +
	    			"Please try again from a new download or contact the author");
	    		System.exit(-1);
	    }
		System.out.println("\n");
		
		//Local dir
		System.out.println("Configuring local installation (questions)");
		String defaultTargetDir=new File(System.getProperty( "user.home" ),InstallerConfig.defaultUserHome).getAbsolutePath();
		targetDir=readLine("Please enter the target directory for the installation of systems files.\n" +
		"Note that this directory is overwritten but not cleared: it is your responsibilty to make sure it is not existent, empty or otherwise consistent\n"+
				"[deafults to "+defaultTargetDir+"]");
		if(targetDir==null || targetDir.trim().equals("")) targetDir=defaultTargetDir;
		System.out.println();
		System.out.println();
		//Instance URI
		String defaultInstanceID=UUID.randomUUID().toString();
		instanceIDString=readLine("Each terminology service instance requires a unieuq identider\n" +
				"If this is a new instance, just accept the default string provided (please note it down)\n"+
				"If you want this installation to have the identity of a previous instance, please provide a new seed string below\n" +
				"Note that if two instances have the same seed synchronization issues may arise\n" +
				"[proposed new ID: "+defaultInstanceID+"]");
		if(instanceIDString==null || instanceIDString.trim().equals("")) instanceIDString=defaultInstanceID;
		System.out.println();
		
		//User URI
		String defaultUserID="http://"+instanceIDString+"/"+System.getProperty("user.name");
		userID=readLine("Please enter a URI for the default user of this system\n" +
				"(Users can be associated to more than one system)" +
				"[proposed user ID: "+defaultUserID+"]");
		if(userID==null || userID.trim().equals("")) userID=defaultUserID;
		System.out.println();
		
		//baseDir
		while(baseURL==null) {
			baseURL=readLine("Please provide the baseURL for this terminology instance.\n" +
					"The baseURL is relative to this server and can differ from the base for URIs of terminologies (see documentation for mode details)\n" +
					"It is used to provide an online publishing of the terminology (it is not used if only pdf exports are needed)\n" +
					"This value must be provided!");
			if(baseURL.trim().length()==0) baseURL=null;
		}
		System.out.println();
		
		//baseURL
		while(baseDisk==null) {
			baseDisk=readLine("Please provide the base directory where a file representation of the terminology server can be created (usually a diectory accessible by Apache or another web server).\n" +
					"This value is used only if the terminology is published as a set of web files (see documentation for more details)\n" +
					"This value must be provided!");
			if(baseDisk.trim().length()==0) baseDisk=null;
		}
		System.out.println();
		
		//pdfLatex
		/*
		pdfLatexFile=readLine("If want authomatic creation of pdf files, please provide the abosulte path of the pdflatex command\n" +
				"The system generates latex and will simply call this command for the final conversion, it is your responsability to be sure you have a functioning Tex system in place. \n");
		if(pdfLatexFile!=null) {
			if(!(new File(pdfLatexFile).exists())) {
				System.out.println("Invalid file, skipping it! You will be able to add the correct command by editing the config files (see documentation)");
			}
		} 
		*/
		pdfLatexFile="NOT_IMPLEMENTED";
		System.out.println();
		System.out.println("Beginning installation");
		
		
		//Please note: refer to README.txt for default configuration options.
		//To run commands, set the $TMSERVER Home Systems Variable first (defaults to user.home/.tserver) 
		//or use the command tsm.bat provided (which hard-codes absolute pathas internally).
		
		makeDirs();
	}
	private static boolean verify() {
		System.out.println("Verifying package integrity");
	    packageDirectory=System.getProperty("user.dir");
	    
	    sourceCommandDirFile=verifyDirExist(new String[]{packageDirectory,InstallerConfig.commandDir});
	    if(sourceCommandDirFile==null) return false;
	    
	    sourceLibDirFile=verifyDirExist(new String[]{packageDirectory,InstallerConfig.libDir});
	    if(sourceLibDirFile==null) return false;
	    
	    sourceWebDirFile=verifyDirExist(new String[]{packageDirectory,InstallerConfig.webDir});
	    if(sourceWebDirFile==null) return false;
	    
	    sourceResourcesDirFile=verifyDirExist(new String[]{packageDirectory,InstallerConfig.resourcesDir});
	    if(sourceResourcesDirFile==null) return false;
	   
	    sourceConfigSubDirFile=verifyDirExist(new String[]{packageDirectory,InstallerConfig.resourcesDir,InstallerConfig.confSubDir});
	    if(sourceConfigSubDirFile==null) return false;
	    
	    sourceTemplatesSubDirFile=verifyDirExist(new String[]{packageDirectory,InstallerConfig.resourcesDir,InstallerConfig.templatesSubDir});
	    if(sourceTemplatesSubDirFile==null) return false;
	    	    
	    for(int i=0;i<InstallerConfig.mustHaveConfTemplates.length;i++) {
	    		mustHaveConfTemplatesFiles[i]=verifyFileExist(new String[]{packageDirectory,InstallerConfig.resourcesDir,InstallerConfig.confSubDir,InstallerConfig.mustHaveConfTemplates[i]});
	    		if(mustHaveConfTemplatesFiles[i]==null) return false;
	    }
	    return true;
	}
	
	private static File verifyDirExist(String...fileBits) {
		String fName=makeFileName(fileBits);
		File dir=new File(fName);
		if(dir.exists() && dir.isDirectory()) {
			System.out.println("DIR: "+fName+" ... "+"OK");
			return dir;
		}
		else {
			System.out.println("DIR: "+fName+" ... "+"NOT FOUND!");
			return null;
		}
	}
	
	private static File verifyFileExist(String...fileBits) {
		String fName=makeFileName(fileBits);
		File file=new File(fName);
		if(file.exists() && file.isFile()) {
			System.out.println("FILE: "+fName+" ... "+"OK");
			return file;
		}
		else {
			System.out.println("FILE: "+fName+" ... "+"NOT FOUND!");
			return null;
		}
	}
	
	private static String makeFileName(String[] fileBits) {
		StringBuilder fnNameBuilder=new StringBuilder();
		for(String bit:fileBits) {
			fnNameBuilder.append(bit);
			fnNameBuilder.append(File.separator);
		}
		fnNameBuilder.setLength(fnNameBuilder.length() - 1);
		return fnNameBuilder.toString();
	}
	
	private static String readLine(String prompt) {
		System.out.print(prompt+ " : ");
		String result=null;
		try {
			result = br.readLine();
		} catch (IOException ioe) {
			System.out.println("Cannot read input");
		    System.exit(1);
		}
		return result;
	} 

	private static void makeDirs() {
		boolean dirResult=true;
		dirResult=dirResult&&checkOrCreateDirectory(targetDir);
		targetDBDir=makeFileName(new String[]{targetDir,InstallerConfig.targetDBString});
		targetCacheDir=makeFileName(new String[]{targetDir,InstallerConfig.targetCacheString});
		targetGitDir=makeFileName(new String[]{targetDir,InstallerConfig.targetGitString});
		targetConfDir=makeFileName(new String[]{targetDir,InstallerConfig.targetConfString});
		targetAuthDir=makeFileName(new String[]{targetDir,InstallerConfig.targetAuthString});
		targetWebDir=makeFileName(new String[]{targetDir,InstallerConfig.targetWebString});
		targetTemplatesDir=makeFileName(new String[]{targetDir,InstallerConfig.targetTemplatesString});
		targetLibDir=makeFileName(new String[]{targetDir,InstallerConfig.targetLibDirString});
		targetCommandDir=makeFileName(new String[]{targetDir,InstallerConfig.targetCommandDirString});
		
		dirResult=dirResult&&checkOrCreateDirectory(targetDBDir);
		dirResult=dirResult&&checkOrCreateDirectory(targetCacheDir);
		dirResult=dirResult&&checkOrCreateDirectory(targetGitDir);
		dirResult=dirResult&&checkOrCreateDirectory(targetConfDir);
		dirResult=dirResult&&checkOrCreateDirectory(targetAuthDir);
		dirResult=dirResult&&checkOrCreateDirectory(targetWebDir);
		dirResult=dirResult&&checkOrCreateDirectory(targetTemplatesDir);
		dirResult=dirResult&&checkOrCreateDirectory(targetLibDir);
		dirResult=dirResult&&checkOrCreateDirectory(targetCommandDir);
		if(!dirResult) {
			System.out.println("ERROR: Problems in creating the directory structure");
			System.exit(-1);
		}
		System.out.println("Copying files");
		boolean copyFilesResult=true;
		copyFilesResult=copyFilesResult&&plainCopyDir(sourceWebDirFile,new File(targetWebDir));
		copyFilesResult=copyFilesResult&&plainCopyDir(sourceLibDirFile,new File(targetLibDir));
		copyFilesResult=copyFilesResult&&plainCopyDir(sourceCommandDirFile,new File(targetCommandDir));
		copyFilesResult=copyFilesResult&&plainCopyDir(sourceTemplatesSubDirFile,new File(targetTemplatesDir)); // TODO must be recursive!!!

		if(!copyFilesResult) {
			System.out.println("ERROR: Problems in copying files from the installer");
			System.exit(-1);
		}
		
		System.out.println();
		System.out.println("Expanding configuration files");
		File[] confFiles=sourceConfigSubDirFile.listFiles();
		for(File confFile:confFiles) {
			File outFile=null;
			if(confFile.getName().startsWith(InstallerConfig.authFilePrefix))
				outFile=new File(targetAuthDir,confFile.getName());
			else if(confFile.getName().equals(InstallerConfig.prefixFileName))
				outFile=new File(targetDir,confFile.getName());
			else if(confFile.getName().equals(InstallerConfig.seedFileName))
				outFile=new File(targetDir,confFile.getName());
			else if(confFile.getName().equals(InstallerConfig.commandName)) {
				outFile=new File(targetDir,confFile.getName());
				outFile.setExecutable(true);
			}
				
			else outFile=new File(targetConfDir,confFile.getName());
			copyFilesResult=copyFilesResult&&expandCopyFile(confFile,outFile);
			if(!copyFilesResult) {
				System.out.println("ERROR: Problems in expanding files from the installer");
				System.exit(-1);
			}
		}
		System.out.println("Installation successful");
		System.out.println();
		System.out.println("You can now use the ts.jar command (a .bat wrapper is provided)");
		System.out.println("Remember to set the $TSHOME variable to your chosen location, or the system will look into the proposed default\n");
		
	}
	
	private static boolean checkOrCreateDirectory (String dirString)  {
		File dirFile=new File(dirString);
		if(dirFile.exists()) {
			if(dirFile.isDirectory()) {
				System.out.println("DIR: "+dirString+" FOUND");
			}
			else {
				System.out.println("DIR: "+dirString+" CONFLICT with omonimous file");
				return false;
			}
		}
		else {
			dirFile.mkdir();
			System.out.println("DIR: "+dirString+" MADE");
		}
		return true;
	}
	
	private static boolean plainCopyDir(File sourceDir, File targetDir ) {
		System.out.println("Copying files from "+sourceDir.getAbsolutePath()+" to "+targetDir.getAbsolutePath());
		File[] sourceFiles=sourceDir.listFiles();
		for(File fileToCopy:sourceFiles) {
			if(!fileToCopy.isDirectory()) {
				System.out.println("Copying: "+fileToCopy.getName());
				try {
					InputStream in = new FileInputStream(fileToCopy);
					OutputStream out = new FileOutputStream(new File(targetDir,fileToCopy.getName()));
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					in.close();
					out.close(); 
				
					//Files.copy( fileToCopy, new File(target,fileToCopy.getName())); TODO Java 1.7 !
					//TODO or we could use Apache Commons, but seems overkill for just this
				} catch (IOException e) {
					System.out.println("Problems in copying "+fileToCopy);
					e.printStackTrace();
					return false;
				} 
			}
			else {
				// recursive step
				File newSourceDir=fileToCopy;
				File newTargetDir=new File(targetDir,fileToCopy.getName());
				newTargetDir.mkdir();
				plainCopyDir(newSourceDir,newTargetDir);
						
			}
		}
		return true;

	}
	
	private static boolean expandCopyFile(File sourceFile, File targetFile ) {
		System.out.println("Expanding "+sourceFile.getAbsolutePath()+" to "+targetFile.getAbsolutePath());
		try {
			BufferedReader br = new BufferedReader(new FileReader(sourceFile));
			BufferedWriter wr = new BufferedWriter(new FileWriter(targetFile));
			String line;
		
			while ((line = br.readLine()) != null) {
			   line=expandTemplateLine(line);
			   wr.write(line+"\n");
			}
			br.close();
			wr.close();
		} catch (IOException e) {
			System.out.println("Problem in expanding file");
			e.printStackTrace();
			return false;
		}
		
		
		
		return true;
			

	}
	
	private static String expandTemplateLine(String line) {
		line=line.replace("<<userID>>", "<"+userID+">");
		line=line.replace("<<tdbDir>>", targetDBDir);
		line=line.replace("<<baseURL>>", baseURL);
		line=line.replace("<<baseDisk>>", baseDisk);
		line=line.replace("<<instanceID>>", instanceIDString);
		line=line.replace("<<pdfLatexCommand>>", pdfLatexFile);
		line=line.replace("<<authDir>>", targetAuthDir);
		line=line.replace("<<cacheDir>>", targetCacheDir);
		line=line.replace("<<templatesDir>>", targetTemplatesDir);
		line=line.replace("<<webDir>>", targetWebDir);
		line=line.replace("<<gitDir>>", targetGitDir);
		line=line.replace("<<prefixFile>>", new File(targetDir,InstallerConfig.prefixFileName).getAbsolutePath());
		line=line.replace("<<seedFile>>", new File(targetDir,InstallerConfig.seedFileName).getAbsolutePath());
		line=line.replace("<<commandFile>>", new File(targetCommandDir,"ts.jar").getAbsolutePath());
		
		return line;
	}
	
}

