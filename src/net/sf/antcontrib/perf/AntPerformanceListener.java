/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 Ant-Contrib project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Ant-Contrib project (http://sourceforge.net/projects/ant-contrib)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The name Ant-Contrib must not be used to endorse or promote products 
 *    derived from this software without prior written permission. For
 *    written permission, please contact
 *    ant-contrib-developers@lists.sourceforge.net.
 *
 * 5. Products derived from this software may not be called "Ant-Contrib"
 *    nor may "Ant-Contrib" appear in their names without prior written
 *    permission of the Ant-Contrib project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE ANT-CONTRIB PROJECT OR ITS
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */
//package ise.antelope.common;
package net.sf.antcontrib.perf;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Comparator;
import java.io.File;
import java.io.FileWriter;

/**
 * This BuildListener keeps track of the total time it takes for each target and
 * task to execute, then prints out the totals when the build is finished. This
 * can help pinpoint the areas where a build is taking a lot of time so
 * optimization efforts can focus where they'll do the most good. Execution times
 * are grouped by targets and tasks, and are sorted from fastest running to
 * slowest running.
 *
 * Output can be saved to a file by setting a property in Ant. Set
 * "performance.log" to the name of a file. This can be set either on the
 * command line with the -D option (-Dperformance.log=/tmp/performance.log)
 * or in the build file itself (<property name="performance.log"
 * location="/tmp/performance.log"/>).
 * <p>Developed for use with Antelope, migrated to ant-contrib Oct 2003.
 *
 * @author Dale Anson, danson@germane-software.com
 * @version $Revision: 1.2 $
 */
public class AntPerformanceListener implements BuildListener {

    private HashMap targetStats = new HashMap();    // key is Target, value is StopWatch
    private HashMap taskStats = new HashMap();      // key is Task, value is StopWatch
    private StopWatch master = null;
    private long start_time = 0;

    /**
     * Starts a 'running total' stopwatch.
     */
    public void buildStarted( BuildEvent be ) {
        master = new StopWatch();
        start_time = master.start();
    }

    /**
     * Sorts and prints the results.
     */
    public void buildFinished( BuildEvent be ) {
        long stop_time = master.stop();

        // sort targets, key is StopWatch, value is Target
        TreeMap sortedTargets = new TreeMap( new StopWatchComparator() );
        Iterator it = targetStats.keySet().iterator();
        while ( it.hasNext() ) {
            Object key = it.next();
            Object value = targetStats.get( key );
            sortedTargets.put( value, key );
        }

        // sort tasks, key is StopWatch, value is Task
        TreeMap sortedTasks = new TreeMap( new StopWatchComparator() );
        it = taskStats.keySet().iterator();
        while ( it.hasNext() ) {
            Object key = it.next();
            Object value = taskStats.get( key );
            sortedTasks.put( value, key );
        }

        // print the sorted results
        StringBuffer msg = new StringBuffer();
        String lSep = System.getProperty( "line.separator" );
        msg.append( lSep ).append("Statistics:").append( lSep );
        msg.append( "-------------- Target Results ---------------------" ).append( lSep );
        it = sortedTargets.keySet().iterator();
        while ( it.hasNext() ) {
            StopWatch key = ( StopWatch ) it.next();
            Target target = ( Target ) sortedTargets.get( key );
            StringBuffer sb = new StringBuffer();
            if ( target.getProject().getName() != null )
                sb.append( target.getProject().getName() ).append( "." );
            String total = format( key.total() );
            sb.append( target.getName() ).append( ": " ).append( total );
            msg.append( sb.toString() ).append( lSep );
        }
        msg.append( lSep );
        msg.append( "-------------- Task Results -----------------------" ).append( lSep );
        it = sortedTasks.keySet().iterator();
        while ( it.hasNext() ) {
            StopWatch key = ( StopWatch ) it.next();
            Task task = ( Task ) sortedTasks.get( key );
            StringBuffer sb = new StringBuffer();
            Target target = task.getOwningTarget();
            if ( target.getProject().getName() != null )
                sb.append( target.getProject().getName() ).append( "." );
            sb.append( target.getName() ).append( "." );
            sb.append( task.getTaskName() ).append( ": " ).append( format( key.total() ) );
            msg.append( sb.toString() ).append( lSep );
        }

        msg.append( lSep );
        msg.append( "-------------- Totals -----------------------------" ).append( lSep );
        SimpleDateFormat format = new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss.SSS" );
        msg.append( "Start time: " + format.format( new Date( start_time ) ) ).append( lSep );
        msg.append( "Stop time: " + format.format( new Date( stop_time ) ) ).append( lSep );
        msg.append( "Total time: " + format( master.total() ) ).append( lSep );
        System.out.println( msg.toString() );
        
        // write stats to file?
        Project p = be.getProject();
        File outfile = null;
        if ( p != null ) {
            String f = p.getProperty( "performance.log" );
            if ( f != null )
                outfile = new File( f );
        }
        if ( outfile != null ) {
            try {
                FileWriter fw = new FileWriter( outfile );
                fw.write( msg.toString() );
                fw.flush();
                fw.close();
                System.out.println( "Wrote stats to: " + outfile.getAbsolutePath() + lSep);
            }
            catch ( Exception e ) {
                // ignored
            }
        }

        // reset the stats registers

        targetStats = new HashMap();
        taskStats = new HashMap();
    }

    /**
     * Formats the milliseconds from a StopWatch into decimal seconds.    
     */
    private String format( long ms ) {
        String total = String.valueOf( ms );
        String frontpad = "000";
        int pad_length = 3 - total.length();
        if ( pad_length >= 0 )
            total = "0." + frontpad.substring( 0, pad_length ) + total;
        else {
            total = total.substring( 0, total.length() - 3 ) + "." + total.substring( total.length() - 3 );
        }
        return total + " sec";
    }

    /**
     * Start timing the given target.    
     */
    public void targetStarted( BuildEvent be ) {
        StopWatch sw = new StopWatch();
        sw.start();
        targetStats.put( be.getTarget(), sw );
    }

    /**
     * Stop timing the given target.    
     */
    public void targetFinished( BuildEvent be ) {
        StopWatch sw = ( StopWatch ) targetStats.get( be.getTarget() );
        sw.stop();
    }

    /**
     * Start timing the given task.    
     */
    public void taskStarted( BuildEvent be ) {
        StopWatch sw = new StopWatch();
        sw.start();
        taskStats.put( be.getTask(), sw );
    }

    /**
     * Stop timing the given task.    
     */
    public void taskFinished( BuildEvent be ) {
        StopWatch sw = ( StopWatch ) taskStats.get( be.getTask() );
        sw.stop();
    }

    /**
     * no-op    
     */
    public void messageLogged( BuildEvent be ) {
        // does nothing
    }

    /**
     * Compares the total times for two StopWatches.    
     */
    public class StopWatchComparator implements Comparator {
        /**
         * Compares the total times for two StopWatches.    
         */
        public int compare( Object o1, Object o2 ) {
            StopWatch a = ( StopWatch ) o1;
            StopWatch b = ( StopWatch ) o2;
            if ( a.total() < b.total() )
                return -1;
            else if ( a.total() == b.total() )
                return 0;
            else
                return 1;
        }
    }

    /**
     * A stopwatch, useful for 'quick and dirty' performance testing.
     * @author Dale Anson
     * @version   $Revision: 1.2 $
     */
    public class StopWatch {

        /**
         * storage for start time
         */
        private long _start_time = 0;
        /**
         * storage for stop time
         */
        private long _stop_time = 0;

        /**
         * cumulative elapsed time
         */
        private long _total_time = 0;

        /**
         * Starts the stopwatch.
         */
        public StopWatch() {
            start();
        }

        /**
         * Starts/restarts the stopwatch.
         *
         * @return   the start time, the long returned System.currentTimeMillis().
         */
        public long start() {
            _start_time = System.currentTimeMillis();
            return _start_time;
        }

        /**
         * Stops the stopwatch.
         *
         * @return   the stop time, the long returned System.currentTimeMillis().
         */
        public long stop() {
            long stop_time = System.currentTimeMillis();
            _total_time += stop_time - _start_time;
            _start_time = 0;
            _stop_time = 0;
            return stop_time;
        }

        /**
         * Total cumulative elapsed time.
         *
         * @return   the total time
         */
        public long total() {
            return _total_time;
        }

        /**
         * Elapsed time, difference between the last start time and now.
         *
         * @return   the elapsed time
         */
        public long elapsed() {
            return System.currentTimeMillis() - _start_time;
        }
    }

    // quick test for the formatter
    public static void main ( String[] args ) {
        AntPerformanceListener apl = new AntPerformanceListener();

        System.out.println( apl.format( 1 ) );
        System.out.println( apl.format( 10 ) );
        System.out.println( apl.format( 100 ) );
        System.out.println( apl.format( 1000 ) );
        System.out.println( apl.format( 100000 ) );
        System.out.println( apl.format( 1000000 ) );
        System.out.println( apl.format( 10000000 ) );
        System.out.println( apl.format( 100000000 ) );
        System.out.println( apl.format( 1000000000 ) );
    }
}
