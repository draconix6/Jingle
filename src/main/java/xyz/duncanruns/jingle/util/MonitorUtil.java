package xyz.duncanruns.jingle.util;

import com.sun.jna.platform.win32.WinDef;
import xyz.duncanruns.jingle.win32.GDI32Extra;
import xyz.duncanruns.jingle.win32.User32;

import java.awt.*;

public final class MonitorUtil {
    public static int minY = 0;
    public static float scaleFactor = 1.0f;

    private MonitorUtil() {
    }

    public static Monitor getPrimaryMonitor() {
        // Java 8 fails to account for the "Scale" setting in Windows display settings on its own
        if (System.getProperty("java.version").startsWith("1.8"))
        {
            // https://stackoverflow.com/questions/5977445/how-to-get-windows-display-settings
            WinDef.HDC hdc = User32.INSTANCE.GetWindowDC(null);
            int virtualHeight = GDI32Extra.INSTANCE.GetDeviceCaps(hdc, 10);
            int physicalHeight = GDI32Extra.INSTANCE.GetDeviceCaps(hdc, 117);
            User32.INSTANCE.ReleaseDC(null, hdc);

            scaleFactor = Math.round((float) physicalHeight / (float) virtualHeight * 100.f) / 100.f;
        }

        GraphicsDevice defaultScreenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Rectangle vBounds = defaultScreenDevice.getDefaultConfiguration().getBounds();

        return new Monitor(
                true,
                vBounds.x,
                vBounds.y,
                (int) Math.floor(vBounds.width / scaleFactor),
                (int) Math.floor(vBounds.height / scaleFactor),
                defaultScreenDevice.getDisplayMode().getWidth(),
                defaultScreenDevice.getDisplayMode().getHeight()
        );
    }

    public static Monitor[] getAllMonitors() {
        final GraphicsDevice[] graphicsDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        Monitor[] monitors = new Monitor[graphicsDevices.length];
        Rectangle primaryMonitorVBounds = getPrimaryMonitor().getVBounds();
        for (int i = 0; i < monitors.length; i++) {
            Rectangle vBounds = graphicsDevices[i].getDefaultConfiguration().getBounds();
            DisplayMode dm = graphicsDevices[i].getDisplayMode();
            monitors[i] = new Monitor(
                    vBounds.equals(primaryMonitorVBounds), vBounds.x, vBounds.y,
                    vBounds.width, vBounds.height,
                    dm.getWidth(), dm.getHeight()
            );
        }
        return monitors;
    }

    public static void retrieveMinY() {
        minY = 0;
        for (Monitor monitor : getAllMonitors()) {
            if (monitor.y < minY) minY = monitor.y;
        }
    }

    public static class Monitor {
        public final boolean isPrimary;
        public final int x;
        public final int y;
        public final int vWidth;
        public final int vHeight;
        public final int pWidth;
        public final int pHeight;

        private Monitor(boolean isPrimary, int x, int y, int vWidth, int vHeight, int pWidth, int pHeight) {
            this.isPrimary = isPrimary;
            this.x = x;
            this.y = y;
            this.vWidth = vWidth;
            this.vHeight = vHeight;
            this.pWidth = pWidth;
            this.pHeight = pHeight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;

            Monitor monitor = (Monitor) o;
            return this.isPrimary == monitor.isPrimary && this.x == monitor.x && this.y == monitor.y && this.vWidth == monitor.vWidth && this.vHeight == monitor.vHeight && this.pWidth == monitor.pWidth && this.pHeight == monitor.pHeight;
        }

        @Override
        public String toString() {
            return "Monitor{" +
                    "isPrimary=" + this.isPrimary +
                    ", x=" + this.x +
                    ", y=" + this.y +
                    ", vWidth=" + this.vWidth +
                    ", vHeight=" + this.vHeight +
                    ", pWidth=" + this.pWidth +
                    ", pHeight=" + this.pHeight +
                    '}';
        }

        /**
         * Gets the virtual bounds of the monitor (considers window scaling)
         */
        public Rectangle getVBounds() {
            return new Rectangle(this.x, this.y, this.vWidth, this.vHeight);
        }

        /**
         * Gets the physical bounds of the monitor (doesn't consider window scaling)
         */
        public Rectangle getPBounds() {
            return new Rectangle(this.x, this.y, this.pWidth, this.pHeight);
        }
    }
}
