package org.example;

/**
 * Simple class for pipeline check.
 * Final for inheritance ban.
 */
public final class PipelineChecker {
    static void main() {
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
