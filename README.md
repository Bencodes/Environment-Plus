Environment-Plus
==============================

A wrapper around [Environment](http://developer.android.com/reference/android/os/Environment.html) that adds support for finding all available external storage paths.

Common Storage Paths ([Source](http://stackoverflow.com/questions/13976982/android-removable-storage-external-sdcard-path-by-manufacturers))
-----------
- /emmc
- /mnt/sdcard/external_sd
- /mnt/external_sd
- /sdcard/sd
- /mnt/sdcard/bpemmctest
- /mnt/sdcard/_ExternalSD
- /mnt/sdcard-ext
- /mnt/Removable/MicroSD
- /Removable/MicroSD
- /mnt/external1
- /mnt/extSdCard
- /mnt/extsd 
- (Some sort of conformity would be nice.)




I was looking for more of a dynamic solution. It took some digging but I found a method inside [StorageManager](http://developer.android.com/reference/android/os/storage/StorageManager.html) that was marked as hidden. I wrote a little wrapper class around it that still supports everything `android.os.Environment` does but adds support for dual sdcards (possibly more).



Usage
-----------

Include this class in your project and:

	public class MyClass extends Activity {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			if(Environment.doesExtraExternalStorageDirectoryExist(this)) {
				// Found Two Or More
			} else {
				// There Is Only One
			}
			
			// Gets All Of The Storage Paths
			final File[] paths = Environment.getExternalStorageList(this);
		}
	}
	
	
	