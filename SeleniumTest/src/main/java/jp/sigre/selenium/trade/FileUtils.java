package jp.sigre.selenium.trade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;
import jp.sigre.LogMessage;

/**
 * @author sigre
 *
 */
public class FileUtils {


	public List<TradeDataBean> csvToTorihikiData(File file) {
		final String[] HEADER = new String[] { "code","dayTime","type","entryMethod","exitMethod","MINI_CHECK_flg","realEntryVolume","entry_money" };
		CSVReader reader = null;
		try {
			reader = new CSVReader(new InputStreamReader(new FileInputStream(file), "SJIS"), ',', '"', 1);
			ColumnPositionMappingStrategy<TradeDataBean> strat = new ColumnPositionMappingStrategy<>();
			strat.setType(TradeDataBean.class);
			strat.setColumnMapping(HEADER);
			CsvToBean<TradeDataBean> csv = new CsvToBean<>();
			return csv.parse(strat, reader);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				reader.close();
			} catch (IOException | NullPointerException e) {
				new LogMessage().writelnLog(e.toString());
			}
		}
	}

	public String[] csvToIdPass(File file) {
		CSVReader reader = null;
		try {

			reader = new CSVReader(new InputStreamReader(new FileInputStream(file), "SJIS"));

			return reader.readNext();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				reader.close();
			} catch (IOException | NullPointerException e) {
				new LogMessage().writelnLog(e.toString());
			}
		}
	}

	public IniBean iniToBean(File file) {
		FileReader fr = null;
		BufferedReader br = null;
		IniBean bean = new IniBean();
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			while ((line = br.readLine()) != null) {
				readIniLine(line, bean);
			}
		} catch (IOException e) {
			new LogMessage().writelnLog(e.toString());
		} finally {
			try {
				br.close();
				fr.close();
			} catch (IOException | NullPointerException e) {
				new LogMessage().writelnLog(e.toString());
			}
		}

		return bean;
	}

	private void readIniLine(String line, IniBean bean) {
		if (line.startsWith("LS_FilePath")) bean.setLS_FilePath(getLS_FilePath(line));
		if (line.startsWith("ID_FilePath")) bean.setID_FilePath(getID_FilePath(line));
		if (line.startsWith("["))			getUseMethod(line, bean);
		if (line.startsWith("Trade_Visible")) bean.setTradeVisible(getTradeVisible(line));
		if (line.startsWith("Sell_UnusedMethod_Immediately")) bean.setSellUnusedMethod(getSellUnusedMethod(line));

	}

	private String getLS_FilePath(String line) {
		return line.split("\"")[1];
	}

	private String getID_FilePath(String line) {
		return line.split("\"")[1];
	}

	private void getUseMethod(String line, IniBean bean) {
		String[] splitLine = line.split("=");
		String[] methods = splitLine[0].split("&");
		String entryMethod = methods[0].substring(1);
		String exitMethod = methods[1].substring(0, methods[1].length()-1);
		String flag = splitLine[1];

		String[] methodSet = {entryMethod, exitMethod, flag};

		bean.addMethodSet(methodSet);
	}

	private String getTradeVisible(String line) {
		return line.split("=")[1];
	}

	private String getSellUnusedMethod(String line) {
		return line.split("=")[1];
	}

	public void makeTradeDataFile(List<TradeDataBean> list, String outPath, boolean isBuying) {

		String fileName = isBuying? "buy_remains.csv" : "sell_remains.csv";

		makeDataFile(list, outPath, fileName);

	}

	public void makeBackupDataFile(List<TradeDataBean> list, String outPath) {

		String fileName = "backup.csv";

		File target = new File(outPath + File.separator + fileName);

		if (target.exists()) {
			if (!target.delete()) {
				new LogMessage().writelnLog(target.getAbsolutePath() + "の削除に失敗しました。");
			}
		}

		makeDataFile(list, outPath, fileName);

	}

	private void makeDataFile(List<TradeDataBean> list, String outPath, String fileName) {
		writeFile("code,dayTime,type,entryMethod,exitMethod,MINI_CHECK_flg,"
				+ "realEntryVolume,entry_money\n", outPath, fileName);

		for (TradeDataBean bean : list) {
			writeFile(bean.toCSV() + "\n", outPath, fileName);
		}
	}


	public void writeFile(String writing,String outPath, String fileName){

		String logFilePath = outPath + File.separator + fileName;

		File file = new File(logFilePath);

		try {
			if (!file.createNewFile()) {
				new LogMessage().writelnLog(file.getAbsolutePath() + "の作成に失敗しました。");
			}

			FileWriter filewriter = new FileWriter(file,true);
			filewriter.write(writing );
			filewriter.close();
		}catch(IOException e){
			new LogMessage().writelnLog(e.toString());
		}

	}

	public String getIdPassFilePath(String strFolderPath) {
		return strFolderPath + File.separator + "idpassword.fbs";
	}

	public String getBuyDataFilePath(String strFolderPath) {

		return strFolderPath + File.separator + getBuyDataFileName();
	}

	public String getSellDataFilePath(String strFolderPath) {

		return strFolderPath + File.separator + getSellDataFileName();
	}

	public String getMovedBuyDataPath(String strFolderPath) {

		return strFolderPath + "\\old\\" + getBuyDataFileName();
	}

	public String getMovedSellDataPath(String strFolderPath) {

		return strFolderPath + "\\old\\" + getSellDataFileName();
	}

	public String getIniPath(String strFolderPath) {

		return strFolderPath + File.separator + "fbs.ini";
	}

	public String getKeyPath(String strFolderPath) {

		return strFolderPath + File.separator + "FBS_KICK_" + getTodayDate() + ".fbs";
	}

	private String getBuyDataFileName() {

		return getTodayDate() + "_L.csv";
	}

	private String getSellDataFileName() {

		return getTodayDate() + "_S.csv";
	}

	private String getTodayDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date dateToday = new Date();

		return sdf.format(dateToday);
	}

	public void removeTradeDataFile(String strLsPath, boolean isBuying) {
		String fileName = isBuying? "buy_remains.csv" : "sell_remains.csv";

		if (!new File(strLsPath + File.separator + fileName).delete()) {
			new LogMessage().writelnLog(fileName + "の削除に失敗しました。");
		}
	}

	public boolean atoshimatsuDataFile(String strLsPath, String strFilePath) throws IOException {
		//TODO：処理終了後のファイル処理をFileUtilsでメソッド化

		String movedPath = new FileUtils().getMovedSellDataPath(strLsPath);

		if (!new File(strLsPath + File.separator + "old").mkdirs()) {
			new LogMessage().writelnLog(strLsPath + "\\oldフォルダの作成に失敗しました。");
		}

		if (!new File(movedPath).exists()) {
			Files.move(Paths.get(strFilePath), Paths.get(movedPath), StandardCopyOption.ATOMIC_MOVE);
			new LogMessage().writelnLog(movedPath + "へのファイルを移動しました。");
		} else {
			if(!new File(strFilePath).delete()) {
				new LogMessage().writelnLog(strFilePath + "の削除に失敗しました。");
			}
		}

		return true;
	}

	public String getExePath(InputStream inputStream, String prefix, String suffix) {
		try {
			java.nio.file.Path p = java.nio.file.Files.createTempFile(prefix, suffix);
			p.toFile().deleteOnExit();
			java.nio.file.Files.copy(inputStream, p, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			return p.toAbsolutePath().toString();
		} catch (IOException e) {
			new LogMessage().writelnLog(e.toString());
			System.exit(1);
		}

		//到達しないはず
		return "";
	}

}
