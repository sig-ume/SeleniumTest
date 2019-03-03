package jp.sigre.fbs.selenium.trade;

import java.util.ArrayList;
import java.util.List;

import jp.sigre.fbs.database.ConnectDB;
import jp.sigre.fbs.log.LogMessage;

/**
 * @author sigre
 *
 */
public class TradeMethodFilter {

	LogMessage log = new LogMessage();

	public void longFilter(List<TradeDataBean> list, IniBean iniBean) {
		List<Boolean> checkbox = new ArrayList<>();

		for (int i = 0; i<list.size(); i++) {
			checkbox.add(false);
		}

		for (int i = 0; i<list.size(); i++) {
			TradeDataBean tradeData = list.get(i);
			String entryMethod = tradeData.getEntryMethod();
			String exitMethod = tradeData.getExitMethod();

			for (String[] methodSet : iniBean.getMethodSet()) {

				if (entryMethod.equals(methodSet[0]) && exitMethod.equals(methodSet[1]) && !methodSet[2].equals("0")) {
					checkbox.set(i, true);
				}
			}

			if(exitMethod.equals("wildcard")) {
				checkbox.set(i, true);
			}
		}

		//チェックボックスでFalse＝選択されてないものを削除
		for (int i = 0; i<list.size(); i++) {
			if (!checkbox.get(i)) {
				list.remove(i);
				checkbox.remove(i);
				i--;
			}
		}

		//株数が0なら削除
		for (int i = 0; i<list.size(); i++) {
			if (list.get(i).getRealEntryVolume().equals("0")) {
				list.remove(i);
				i--;
			}
		}

	}

	public void shortFilter(List<TradeDataBean> list, IniBean iniBean) {

		//sellUnusedMethodが0だった場合、使用するメソッドのみ売却処理→methodでのフィルターをかける
		if (iniBean.getSellUnusedMethod().equals("0")) {
			this.longFilter(list, iniBean);
		} else {

			//株数が0なら削除
			for (int i = 0; i<list.size(); i++) {
				if (list.get(i).getRealEntryVolume().equals("0")) {
					list.remove(i);
					i--;
				}
			}
		}


		for (int i = 0; i < list.size(); i++) {
			for (int j = i+1; j < list.size(); j++) {
				if (list.get(i).equalsCodeMethods(list.get(j))) {
					list.remove(j);
					j--;
				}
			}
		}

		ConnectDB db = new ConnectDB();

		for (int i = 0; i < list.size(); i++ ) {
			TradeDataBean tradeData = list.get(i);
			//処理予定の株を持ってるかチェックをフィルターに
			TradeDataBean dbBean = db.getTradeViewOfCodeMethods(tradeData.getCode(), tradeData.getEntryMethod(), tradeData.getExitMethod());
			if (dbBean.getRealEntryVolume().equals("0")) {
				list.remove(i);
				i--;
			}
		}

	}

	public void skipCode(List<TradeDataBean> list, IniBean iniBean) {
		List<Integer> skipList = iniBean.getSkipList();

		for (int j = 0; j<skipList.size(); j++) {
			for (int i = 0; i<list.size(); i++) {

				int skipNumber = skipList.get(j);
				TradeDataBean bean = list.get(i);
				int code = Integer.parseInt(bean.getCode());
				if (code == skipNumber) {
					list.remove(i);
					i--;
					log.writelnLog(skipNumber + "は設定に従い売買を行いません。");
					continue;
				}
			}
		}
	}

}
