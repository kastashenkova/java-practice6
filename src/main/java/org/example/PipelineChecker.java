package org.example;

/**
 * Simple class for pipeline check.
 * Final for inheritance ban.
 */
public final class PipelineChecker {
    /**
     * Main entry point of the application.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        System.out.println("Pipeline is working");
    }

    /**
     * Simple test method for pipeline check.
     *
     * @return true if pipeline is working
     */
    public boolean isWorking() {
        return true;
    }
}
