package ucsc.lmcghee.rememo;

import java.io.File;

import java.io.FileNotFoundException;

import java.util.LinkedList;

import android.content.Context;


public class LinkedListCreator {

 Context context;
 public LinkedListCreator(Context c){
  context = c;
 }
 
 public LinkedList<File> makeSoundList(String s)
 {
  LinkedList<File> result = new LinkedList<File>();
  //Go to file s
  File directory = new File(context.getExternalFilesDir(s).getAbsolutePath() + 
                File.separator);
  File[] fileArray = directory.listFiles();
  for(File file : fileArray){
   result.add(file);
  }
  //Put all files in there 
  return result;
 }
 public LinkedList<File> makeCategoriesList(){
  LinkedList<File> result = new LinkedList<File>();
  //Go to file s
  File directory = new File(context.getExternalFilesDir(null).getAbsolutePath() + 
                File.separator);
  File[] fileArray = directory.listFiles();
  for(File file : fileArray){
   result.add(file);
  }
  //Put all files in there 
  return result;
 }
}