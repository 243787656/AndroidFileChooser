<h1>Android File Chooser</h1>
<p>Android File Chooser is a simple and customizable file/directory chooser dialog which you can use in your apps to let your users select a file or directory based on your needs.</p>
<h2>How to Add the Library</h2>
<p>This library is availabe in the jcenter repository. Simply add this line of code in your dependencies:</p>
```
compile 'ir.sohreco.androidfilechooser:android-file-chooser:1.0.0@aar'
```
<h2>How to Use</h2>
If you want the default look for your file/directory chooser you can simply implement FileChooserDialog.ChooserListener in your class and create an instance of FileChooserDialog.Builder and then show the dialog:</p>
```java
FileChooserDialog.Builder builder = new FileChooserDialog.Builder(FileChooserDialog.ChooserType.FILE_CHOOSER, this);
```
<p>Notice that the first parameter is the chooser type which you should select from the ChooserType enum and the second parameter is the class that implements FileChooserDialog.ChooserListener </p>
<p>Then you can show your chooser dialog as simple as writing this piece of code:</p>
```java
try {
  builder.build().show(getSupportFragmentManager(), null);
} catch (ExternalStorageNotAvailableException e) {
  e.printStackTrace();
}
```
<p>You should catch ExternalStorageNotAvailableException when you want to make an instance of the fragment by calling build().</p>
<h3>How to Customize</h3>
<p>You can easily customize your chooser with the methods that are provided in FileChooserDialog.Builder class. Note that all of these settings are optional and you can set any of these properties you want.</p>
```java
FileChooserDialog.Builder builder = 
                new FileChooserDialog.Builder(FileChooserDialog.ChooserType.FILE_CHOOSER, this)
                .setTitle("Select a file:")
                .setFileFormats(new String[]{".png", ".jpg"})
                .setFileIcon(R.drawable.ic_file)
                .setDirectoryIcon(R.drawable.ic_directory)
                .setPreviousDirectoryButtonIcon(R.drawable.ic_prev_dir);
```
<h2>Screenshots</h2>
<p>The screenshot below is how the default file chooser looks:</p>
<center><img src="http://sm.uploads.im/t/IhMw3.png" /></center>
<p>And this is how the default directory chooser looks:</p>
<img src="http://sj.uploads.im/t/WENpi.png" />
