package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.boot.*;
import org.eclipse.ui.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import java.util.zip.CRC32;

/**
 * The about info class;
 * <p>
 * The information within this object is obtained from the about "ini" file".
 * This file resides within an install configurations directory and must be a 
 * standard java property file.  
 * </p>
 */
public class AboutInfo extends NewConfigurationInfo {

	private String appName;
	private String productName;
	private ImageDescriptor windowImage;
	private ImageDescriptor aboutImage;
	private ImageDescriptor featureImage;
	private String aboutText;
	private URL welcomePageURL;
	private String featureImageName;
	private Long featureImageCRC;
	private boolean calculatedImageCRC = false;
	private final static int BYTE_ARRAY_SIZE = 2048;

	/**
	 * Constructs a new instance of the about info.
	 */
	public AboutInfo(String featureId, PluginVersionIdentifier versionId) {
		super(featureId, versionId, "about.ini", "about.properties", "about.mappings"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Returns the descriptor for an image which can be shown in an "about" dialog 
	 * for this product.
	 * Products designed to run "headless" typically would not have such an image.
	 * 
	 * @return the descriptor for an about image, or <code>null</code> if none
	 */
	public ImageDescriptor getAboutImage() {
		return aboutImage;
	}

	/**
	 * Returns the descriptor for an image which can be shown in an "about features" 
	 * dialog.
	 * Products designed to run "headless" typically would not have such an image.
	 * 
	 * @return the descriptor for a feature image, or <code>null</code> if none
	 */
	public ImageDescriptor getFeatureImage() {
		return featureImage;
	}

	/**
	 * Returns the name of the feature image as supplied in the properties file.
	 * 
	 * @return the name of the feature image, or <code>null</code> if none
	 */
	public String getFeatureImageName() {
		return featureImageName;
	}

	/**
	 * Returns the CRC of the feature image as supplied in the properties file.
	 * 
	 * @return the CRC of the feature image, or <code>null</code> if none
	 */
	public Long getFeatureImageCRC() {
		if (!calculatedImageCRC && featureImageName != null) {
			featureImageCRC = calculateFeatureImageCRC();
			calculatedImageCRC = true;
		}
		return featureImageCRC;
	}

	/**
	 * Calculate a CRC for the feature image
	 */
	private Long calculateFeatureImageCRC() {
		URL url = null;
		if (featureImageName != null) {
			url = getDescriptor().find(new Path("$nl$").append(featureImageName));
		}
		if (url == null)
			return null;
		// Get the image bytes
		InputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			in = url.openStream();	
			out = new ByteArrayOutputStream();
			byte[] buffer = new byte[BYTE_ARRAY_SIZE];
			int readResult = BYTE_ARRAY_SIZE;
			while (readResult == BYTE_ARRAY_SIZE) {
				readResult = in.read(buffer);
				if (readResult > 0) 
					out.write(buffer, 0, readResult);
			}
			byte[] contents = out.toByteArray();
			// Calculate the crc
			CRC32 crc = new CRC32();
			crc.update(contents);
			return new Long(crc.getValue());
		} catch (IOException e) {
			return null;
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
				}
		}
	}	
		

	/**
	 * Returns a label for the feature, we use the descriptor label
	 */
	public String getFeatureLabel() {
		if (getDescriptor() == null)
			return null;
		return getDescriptor().getLabel();
	}

	/**
	 * Returns the text to show in an "about" dialog for this product.
	 * Products designed to run "headless" typically would not have such text.
	 * 
	 * @return the about text, or <code>null</code> if none
	 */
	public String getAboutText() {
		return aboutText;
	}

	/**
	 * Returns the app name or <code>null</code>.
	 * Note this is never shown to the user.
	 * It is used to initialize the SWT Display.
	 * <p>
	 * On Motif, for example, this can be used
	 * to set the name used for resource lookup.
	 * </p>
	 *
	 * @return the app name, or <code>null</code>
	 * 
	 * @see org.eclipse.swt.widgets.Display#setAppName
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * Returns the product name or <code>null</code>.
	 * This is shown in the window title and the About action.
	 *
	 * @return the product name, or <code>null</code>
	 */
	public String getProductName() {
		IPluginDescriptor desc = getDescriptor();
		if (desc == null)
			return null;
		return desc.getLabel();
	}

	/**
	 * Returns the provider name or <code>null</code>.
	 *
	 * @return the provider name, or <code>null</code>
	 */
	public String getProviderName() {
		IPluginDescriptor desc = getDescriptor();
		if (desc == null)
			return null;
		return desc.getProviderName();
	}

	/**
	 * Returns the version or <code>null</code>.
	 *
	 * @return the version, or <code>null</code>
	 */
	public String getVersion() {
		PluginVersionIdentifier versionId = getVersionId();
		if (versionId == null)
			return null;
		return versionId.toString();
	}

	/**
	 * Returns a <code>URL</code> for the welcome page.
	 * Products designed to run "headless" typically would not have such an page.
	 * 
	 * @return the welcome page, or <code>null</code> if none
	 */
	public URL getWelcomePageURL() {
		return welcomePageURL;
	}

	/**
	 * Returns the image descriptor for the window image to use for this product.
	 * Products designed to run "headless" typically would not have such an image.
	 * 
	 * @return the image descriptor for the window image, or <code>null</code> if none
	 */
	public ImageDescriptor getWindowImage() {
		return windowImage;
	}
	
	/**
	 * Reads the ini file.
	 */
	protected void readINIFile(URL iniURL, URL propertiesURL, URL mappingsURL)
		throws CoreException {

		Properties ini = new Properties();
		InputStream is = null;
		try {
			is = iniURL.openStream();
			ini.load(is);
		} catch (IOException e) {
			reportINIFailure(e, "Cannot read about info file " + iniURL); //$NON-NLS-1$
			return;
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
			}
		}

		PropertyResourceBundle bundle = null;

		if (propertiesURL != null) {
			InputStream bundleStream = null;
			try {
				bundleStream = propertiesURL.openStream();
				bundle = new PropertyResourceBundle(bundleStream);
			} catch (IOException e) {
				reportINIFailure(e, "Cannot read about properties file " + propertiesURL);  //$NON-NLS-1$
				bundle = null;
			} finally {
				try {
					if (bundleStream != null)
						bundleStream.close();
				} catch (IOException e) {
				}
			}
		}

		PropertyResourceBundle mappingsBundle = null;

		if (mappingsURL != null) {
			InputStream bundleStream = null;
			try {
				bundleStream = mappingsURL.openStream();
				mappingsBundle = new PropertyResourceBundle(bundleStream);
			} catch (IOException e) {
				reportINIFailure(e, "Cannot read about mappings file " + mappingsURL);  //$NON-NLS-1$
				mappingsBundle = null;
			} finally {
				try {
					if (bundleStream != null)
						bundleStream.close();
				} catch (IOException e) {
				}
			}
		}

		// Create the mappings array
		ArrayList mappingsList = new ArrayList();
		if (mappingsBundle != null) {
			boolean found = true;
			int i = 0;
			while (found) {
				try {
					mappingsList.add(mappingsBundle.getString(new Integer(i).toString()));
				} catch (MissingResourceException e) {
					found = false;
				}
				i++;
			}
		}
		String[] mappingsArray = (String[])mappingsList.toArray(new String[mappingsList.size()]);

		windowImage = getImage(ini, "windowImage"); //$NON-NLS-1$

		aboutText = (String) ini.get("aboutText"); //$NON-NLS-1$
		aboutText = getResourceString(aboutText, bundle, mappingsArray);

		aboutImage = getImage(ini, "aboutImage"); //$NON-NLS-1$

		featureImageName = (String) ini.get("featureImage"); //$NON-NLS-1$
		featureImage = getImage(ini, "featureImage"); //$NON-NLS-1$

		welcomePageURL = getURL(ini, "welcomePage");

		appName = (String) ini.get("appName"); //$NON-NLS-1$
		appName = getResourceString(appName, bundle, mappingsArray);

	}

	/**
	 * Returns a URL for the given key, or <code>null</code>.
	 * 
	 * @return a URL for the given key, or <code>null</code>
	 */
	private URL getURL(Properties ini, String key) {
		URL url = null;
		String fileName = (String) ini.get(key);
		if (fileName != null) {
			url = getDescriptor().find(new Path("$nl$").append(fileName));
		}
		return url;
	}

	/**
	 * Returns an image descriptor for the given key, or <code>null</code>.
	 * 
	 * @return an image descriptor for the given key, or <code>null</code>
	 */
	private ImageDescriptor getImage(Properties ini, String key) {
		URL url = getURL(ini, key);
		if (url != null) {
			return ImageDescriptor.createFromURL(url);
		}
		return null;
	}

}