package fr.theshark34.technicraft.launcher;

import java.io.File;
import java.io.IOException;

import fr.theshark34.openauth.AuthPoints;
import fr.theshark34.openauth.AuthenticationException;
import fr.theshark34.openauth.Authenticator;
import fr.theshark34.openauth.model.AuthAgent;
import fr.theshark34.openauth.model.response.AuthResponse;
import fr.theshark34.openlauncherlib.launcher.AuthInfos;
import fr.theshark34.openlauncherlib.launcher.GameFolder;
import fr.theshark34.openlauncherlib.launcher.GameInfos;
import fr.theshark34.openlauncherlib.launcher.GameLauncher;
import fr.theshark34.openlauncherlib.launcher.GameTweak;
import fr.theshark34.openlauncherlib.launcher.GameType;
import fr.theshark34.openlauncherlib.launcher.GameVersion;
import fr.theshark34.supdate.BarAPI;
import fr.theshark34.supdate.SUpdate;
import fr.theshark34.supdate.application.integrated.FileDeleter;
import fr.theshark34.swinger.Swinger;

public class Launcher {
	
	public static final GameVersion SC_VERSION = new GameVersion("1.16", GameType.V1_8_HIGHER);
	public static final GameInfos SC_INFOS = new GameInfos("Technicraft V6.5", SC_VERSION, true,new GameTweak[] {GameTweak.FORGE});
	public static final File SC_DIR = SC_INFOS.getGameDir();
	
	private static AuthInfos authInfos;
	private static Thread updateThread;
	
	public static void auth(String username, String password) throws AuthenticationException {
		Authenticator authenticator = new Authenticator(Authenticator.MOJANG_AUTH_URL, AuthPoints.NORMAL_AUTH_POINTS);
		AuthResponse response = authenticator.authenticate(AuthAgent.MINECRAFT, username, password, "");
		authInfos = new AuthInfos(response.getSelectedProfile().getName(), response.getAccessToken(), response.getSelectedProfile().getId());
	}
	
	public static void update() throws Exception {
		SUpdate su = new SUpdate("http://technicraftV6.com/update/", SC_DIR);	 
		su.addApplication(new FileDeleter());
		updateThread = new Thread() {
			private int val;
			private int max;			
			
			@Override
			public void run() {
				while(!this.isInterrupted()) {
					if(BarAPI.getNumberOfFileToDownload() == 0) {
						LauncherFrame.getInstance().getLauncherPanel().setInfoText("Vérification des fichiers");
						continue;
					}
					
					val = (int) (BarAPI.getNumberOfTotalDownloadedBytes() / 1000);
					max = (int) (BarAPI.getNumberOfTotalBytesToDownload() / 1000);
					
					LauncherFrame.getInstance().getLauncherPanel().getProgressBar().setMaximum(max);
					LauncherFrame.getInstance().getLauncherPanel().getProgressBar().setValue(val);
					
					LauncherFrame.getInstance().getLauncherPanel().setInfoText("Téléchargement des fichiers" +
							BarAPI.getNumberOfDownloadedFiles() + "/" + BarAPI.getNumberOfFileToDownload() + " " +
								Swinger.percentage(val, max) + "%");
				}
			}
		};
		updateThread.start();
		
		su.start();
		updateThread.interrupt();
	}
	
	public static void launch() throws IOException {
		GameLauncher gameLauncher = new GameLauncher(SC_INFOS, GameFolder.BASIC, authInfos);
		Process p = gameLauncher.launch();
		try {
			Thread.sleep(5000L);
		} catch(InterruptedException e) {
		}
		LauncherFrame.getInstance().setVisible(false);
		try {
			p.waitFor();
		} catch(InterruptedException e) {
		}
		System.exit(0);
	}
	
	public static void interruptThread() {
		updateThread.interrupt();
	}
}