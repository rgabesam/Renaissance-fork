package org.renaissance.jdk.streams;

//import java.io.IOException;

//import org.renaissance.*;
import org.renaissance.Benchmark;
import org.renaissance.BenchmarkContext;
import org.renaissance.BenchmarkResult;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class JavaFilesCopying implements Benchmark {

    private String workingDirectory = System.getProperty("user.dir");
    private String copyFromFolderPath = workingDirectory + "/benchmarks/jdk-streams/src/main/resources/toCopy/";
    private String copyToFolderPath = workingDirectory + "/benchmarks/jdk-streams/src/main/resources/copied/";
        

    @Override
    public void setUpBeforeEach(BenchmarkContext context) {
        removeCopiedFiles();
    }

    @Override
    public void tearDownAfterAll(BenchmarkContext context) {
        removeCopiedFiles();
    }

    private void removeCopiedFiles(){
        System.out.println("--Removing all copied files--");
        File copyToFolder = new File(copyToFolderPath);
        File[] copiedFiles = copyToFolder.listFiles();

        for (int i = 0; i < copiedFiles.length; i++){
            //System.out.println("removing file: " + copiedFiles[i].getName());
            copiedFiles[i].delete();
        }
    }

    @Override
    public BenchmarkResult run(BenchmarkContext context) {
        
        /**/
        try{
            
            copyFiles(copyFromFolderPath, copyToFolderPath);

        }catch (IOException e){
            System.out.println("Exception raised");
            System.out.println(e.getStackTrace());
        }
        /**/
        return new CopyResult(copyFromFolderPath, copyToFolderPath);
    }

    public void copyFiles(String copyFromPath, String copyToPath) throws IOException {
        File folder = new File(copyFromPath);
        File[] listOfFiles = folder.listFiles();
            
        for (int i = 0; i < listOfFiles.length; i++) {
            String fileName = listOfFiles[i].getName();
        
            Path originalPath = Paths.get(copyFromPath + fileName);
            Path copied = Paths.get(copyToPath + fileName);
            Files.copy(originalPath, copied);
        }
    }
    
    

    class CopyResult implements BenchmarkResult{

        private String copyFromPath;
        private String copyToPath;

        

        public CopyResult(String copyFromPath, String copyToPath) {
            this.copyFromPath = copyFromPath;
            this.copyToPath = copyToPath;
        }



        @Override
        public void validate() throws ValidationException {
            System.out.println("--Validating all files are copied--");

            File originFolder = new File(copyFromPath);
            File[] originFiles = originFolder.listFiles();
            
            File copyToFolder = new File(copyToPath);
            File[] copiedFiles = copyToFolder.listFiles();
            
            if(copiedFiles.length != originFiles.length){
                throw new ValidationException("Not all files are properly copied");
            }

            String[] originFilesNames = new String[originFiles.length];
            String[] copiedFilesNames = new String[copiedFiles.length];
            for (int i = 0; i < originFiles.length; i++){
                originFilesNames[i] = originFiles[i].getName();
                copiedFilesNames[i] = copiedFiles[i].getName();
            }

            Arrays.sort(originFilesNames);
            Arrays.sort(copiedFilesNames);

            try{
                for (int i = 0; i < originFiles.length; i++) {
                    if(!originFilesNames[i].equals(copiedFilesNames[i])){
                        throw new ValidationException("file " + originFiles[i].getName() 
                            + " is not copied, instead file " 
                            + copiedFiles[i].getName() 
                            + " was found");
                    }
                }
            }catch (ValidationException e){
                throw e;
            }catch(Exception e){
                e.printStackTrace();
                throw new ValidationException("Not all files are properly copied");
            }
            

            
        }

    }
}
