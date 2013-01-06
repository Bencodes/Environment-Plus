package com.bencodes.storage;

import android.content.Context;
import android.os.Build;
import android.os.storage.StorageManager;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * An {@link android.os.Environment} wrapper that adds
 * support for phones that have multiple external storage directories.
 */
public class Environment extends android.os.Environment {

	/**
	 * SDCard File List
	 */
	private static File[] mStorageList;


	/**
	 * List Of The Potential Volume Daemons
	 */
	private static final File[] mVolumeDaemonList = new File[] {
			new File(getRootDirectory(), "etc/vold.fstab"),
			new File(getRootDirectory(), "etc/vold.conf")
	};


	/**
	 * Returns an array of files containing the paths to all of the external
	 * storage directories (Emulated/Removable). As a fall back, it reads in the volume daemon file
	 * and parses the contents.
	 * <p/>
	 * <b>Note:</b> This method takes advantage of a hidden method inside {@link StorageManager} which
	 * was not introduced until API 14 (ICE_CREAM_SANDWICH/4.0.1).
	 * <p/>
	 *
	 * @param context {@link Context} used to get StorageManager
	 */
	public static final File[] getExternalStorageList (Context context) {
		mStorageList = null;
		if (mStorageList == null) {
			try {
				mStorageList = (Build.VERSION.SDK_INT >= 14)
						? getExternalStorageList((StorageManager) context.getSystemService(Context.STORAGE_SERVICE))
						: getVolumeDaemonExternalStorageList();
			} catch (Exception e) {
			}
		}

		return mStorageList;
	}


	/**
	 * Checks to see if more than one external storage directory exists.
	 *
	 * @param context {@link Context} used to get StorageManager
	 * @return
	 */
	public static final boolean doesExtraExternalStorageDirectoryExist (Context context) {
		getExternalStorageList(context);
		return mStorageList != null
				? mStorageList.length >= 2
				: false;
	}


	/**
	 * Returns an array of files containing the paths to all of the external
	 * storage directories (Emulated/Removable) provided by the volume daemon
	 * config file.
	 *
	 * @return
	 */
	public static final File[] getVolumeDaemonExternalStorageList () {
		for (File daemon : mVolumeDaemonList) {
			try {
				if (daemon.exists() && daemon.canRead()) {
					final String[] stringArray = readFileIntoStringArray(daemon);
					final List<File> fileList = new ArrayList<File>();
					for (String str : stringArray) {
						final File f = new File(str.split(" ")[2].split(":")[0]);
						if (!doesFileExistInList(fileList, f)) {
							fileList.add(f);
						}
					}
					return (!fileList.isEmpty() ? fileList.toArray(new File[fileList.size()]) : null);
				}
			} catch (Exception e) {
			}
		}

		return null;
	}


	private static final File[] getExternalStorageList (StorageManager storageManager) throws Exception {
		final Method method = storageManager.getClass().getMethod("getVolumePaths");
		final String[] strList = (String[]) method.invoke(storageManager);
		final List<File> fileList = new ArrayList<File>();

		for (String path : strList) {
			final File file = new File(path);
			if (!doesFileExistInList(fileList, file)) {
				fileList.add(file);
			}
		}

		return (!fileList.isEmpty() ? fileList.toArray(new File[fileList.size()]) : null);
	}

	private static final boolean doesFileExistInList (List<File> fileList, File newFile) {
		if (newFile == null || fileList == null) {
			// File Is Null Or List Is Null
			return true;
		}

		if (!newFile.exists()) {
			// The File Doesn't Exist
			return true;
		}

		if (!newFile.isDirectory()) {
			// File Is Not A Directory
			return true;
		}

		if (!newFile.canRead()) {
			// Can't Read The File
			return true;
		}

		if (newFile.getTotalSpace() <= 0) {
			// File Has No Space
			// Filters Usbdisk Out
			return true;
		}

		if (newFile.getName().equalsIgnoreCase("tmp")) {
			// This Folder Showed Up On My Droid X, Filter It Out.
			return true;
		}

		if (fileList.contains(newFile)) {
			// File Is In The List
			return true;
		}

		// Make Sure The File Isn't In The List As A Link Of Some Sort
		// More Of An In Depth Look
		for (File file : fileList) {
			if (file.getFreeSpace() == newFile.getFreeSpace() && file.getUsableSpace() == newFile.getUsableSpace()) {
				// Same Free/Usable Space
				// Must Be Same Files
				return true;
			}
		}

		// File Passed All Of My Tests
		return false;
	}

	private static final String[] readFileIntoStringArray (File file) {
		try {
			final Scanner scanner = new Scanner(file);
			final List<String> stringList = new ArrayList<String>();
			while (scanner.hasNext()) {
				final String line = scanner.nextLine();
				if (line != null) {
					if (line.length() > 0) {
						if (!line.startsWith("#")) {
							stringList.add(line);
						}
					}
				}
			}
			return !stringList.isEmpty() ? stringList.toArray(new String[stringList.size()]) : null;
		} catch (Exception e) {
			return null;
		}
	}
}
