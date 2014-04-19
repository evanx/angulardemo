package iolfeed;

import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author evanx
 */
public class TaskManager {

    LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue(100);
    
    
}
