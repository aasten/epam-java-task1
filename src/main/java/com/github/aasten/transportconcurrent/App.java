package com.github.aasten.transportconcurrent;

import java.util.ResourceBundle;

import com.github.aasten.transportconcurrent.system.TransportSystem;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final String PROPERTY_FILE_NAME = "in"; 
    
    public static void main( String[] args )
    {
        TransportSystem.execute(ResourceBundle.getBundle(PROPERTY_FILE_NAME));
    }
}
