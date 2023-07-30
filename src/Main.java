import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Main implements NativeKeyListener {

    Robot robot;
    //private int launchDelay = 5000; // Delay in ms (1000ms = 1s)
    private int autoClickDelay = 100; // Click x times per second (1000 -> 1 Click Per Second | 100 -> 10 Clicks per Second)
    private int upgradeDelay = 1000; // Try to purchase Upgrade every x ms (1000 -> 1 Upgrade/sec)
    private ArrayList<Integer> upgradeType; // List containing Upgrade Types, remove KeyEvent below to not include
    private boolean paused = false;

    public Main() throws AWTException {
        upgradeType = new ArrayList<>() {{
            add(KeyEvent.VK_A);
            add(KeyEvent.VK_S);
            add(KeyEvent.VK_D);
            add(KeyEvent.VK_F);
            add(KeyEvent.VK_G);
        }};
        robot = new Robot();
    }

    public static void main(String[] args) throws AWTException {
        Main main = new Main();

        try {
            GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(main);

        main.initThreads();
    }

    private void initThreads() {
        new Thread(() -> {
            autoClick();
        }).start();

        new Thread(() -> {
            autoUpgrade();
        }).start();
    }

    // Purchase an Upgrade every x ms, rotates from first Upgrade to Last in List -> Repeat
    private void autoUpgrade() {
        while (!paused) {
            for (int currentUpgradeType:upgradeType) {
                robot.keyPress(currentUpgradeType);
                robot.delay(upgradeDelay);
                robot.keyRelease(currentUpgradeType);
            }
        }
    }

    // Automatically Click every x ms
    private void autoClick() {
        while (!paused) {
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.delay(autoClickDelay);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

        if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
            try {
                GlobalScreen.unregisterNativeHook();
                System.exit(0);
            } catch (NativeHookException nativeHookException) {
                nativeHookException.printStackTrace();
            }
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_P) {
            paused = !paused;
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
    }
}
