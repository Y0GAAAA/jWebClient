package tests;

import java.io.OutputStream;

/**
 * An output stream placeholder to use in tests where response is not useful.
 */
class AimlessOutputStream extends OutputStream {
    
    @Override
    public void write(int b) {}
    
}
