/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.utilities;

import javafx.application.Platform;

/**
 *
 * @author edisondelgado
 */
public abstract class AsyncTask {

	private boolean daemon = true;

	public abstract void onPreExecute();

	public abstract void doInBackground();

	public abstract void onPostExecute();

	public abstract void progressCallback(Object... params);

	public void publishProgress(final Object... params) {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				progressCallback(params);
			}
		});
	}

	private final Thread backGroundThread = new Thread(new Runnable() {

		@Override
		public void run() {

			doInBackground();

			Platform.runLater(new Runnable() {

				@Override
				public void run() {

					onPostExecute();
				}
			});
		}
	});

	public void execute() {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				onPreExecute();

				backGroundThread.setDaemon(daemon);
				backGroundThread.start();
			}
		});
	}

	public void setDaemon(boolean daemon) {

		this.daemon = daemon;
	}

	public void interrupt() {

		this.backGroundThread.interrupt();
	}
}