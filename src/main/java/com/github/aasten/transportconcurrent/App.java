package com.github.aasten.transportconcurrent;

import java.io.File;

import com.github.aasten.transportconcurrent.system.TransportSystem;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        TransportSystem.execute(new File("/tmp/1"));
    }
}
