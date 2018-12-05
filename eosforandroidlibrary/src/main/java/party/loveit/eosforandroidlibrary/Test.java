package party.loveit.eosforandroidlibrary;

import java.util.ArrayList;
import java.util.List;

import party.loveit.eosforandroidlibrary.rpc.exception.ApiException;
import party.loveit.eosforandroidlibrary.rpc.vo.transaction.Transaction;


public class Test {

	static final String eosjs_transfer_seriz = "00f2d4142123e95d0000c85353840ccdb486010000000000045359530000000019e6b58be8af95313233616263646f2e2f2c2e2f214023232425";

	static final String eosjs_account_seriz = "0000000000ea30550002a2f164772b5601000000010003ee4221c9c3f4f62646e3c758dbb8abaae506a559f67148a76968fa6b0f0868140100000001000000010003ba8de2f029cae85e7ca5c9f591bb17b86d750c5116cec30d94100e16e446d41501000000";

	public static void main(String[] args) throws Exception{
		System.out.println("******************* Ecc Start *******************\n");
		
		
		System.out.println("============= 通过种子生成私钥 ===============");
		String pk = Ecc.seedPrivate("adsadsadsadsadsa".getBytes("utf-8"));
		System.out.println("private key :" + pk +"\n");

		System.out.println("============= 通过私钥生成公钥 ===============");
		String pu = Ecc.privateToPublic(pk);
		System.out.println("public key :" + pu + " \n ");

		System.out.println("============= 自定义数据签名 ===============");
		String sign = Ecc.sign(pk,"is京東價as看到可可是是是@#￥%……&*（CVBNM《d ");
		System.out.println("sign :" + sign + " \n ");
		
		System.out.println("============= 转账数据序列化 ===============");
		String data = Ecc.parseTransferData("fromaccount", "toaccount", "10.0020 SYS", "测试123abcdo./,./!@##$%");
		System.out.println("seriz data :" + data);
		System.out.println("transfer eq eosjs seriz " + data.equals(eosjs_transfer_seriz)+" \n ");

		System.out.println("============= 创建账户数据序列化 ===============");
		String data1 = Ecc.parseAccountData("eosio", "espritbloc1.","EOS8eAX54cJtAngV2V22WZhRCW7e4sTAZz1mC5U22vp8mAGuFdMXx","EOS8FPooohZiiCAYXahWCQRxgXXzUbS2gNELAeYCUgGdDMbd2FHQT");
		System.out.println("seriz data :" + data1);
		System.out.println("account eq eosjs seriz " + data1.equals(eosjs_account_seriz));

		
		System.out.println("\n******************* Ecc End *******************\n\n\n");
		
		System.out.println("******************* Rpc Start *******************\n");
		
		Rpc rpc = new Rpc("http://47.106.221.171:8888");
		
		System.out.println("============= 转账 ===============");
		try {
			Transaction t1 = rpc.transfer("5KQwrPbwdL6PhXujxW37FSSQZ1JiwsST4cqQzDeyXtP79zkvFD3","espritblocke", "inita","initb", "12.2821 MSP", "");
			System.out.println("转账成功 = " + t1.getTransactionId()+" \n ");
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		System.out.println("============= 创建账户并且抵押 ===============");
		try {	
			Transaction t2 = rpc.createAccount("5KQwrPbwdL6PhXujxW37FSSQZ1JiwsST4cqQzDeyXtP79zkvFD3","eosio","ccccc..bbbbb", "EOS8eAX54cJtAngV2V22WZhRCW7e4sTAZz1mC5U22vp8mAGuFdMXx","EOS8eAX54cJtAngV2V22WZhRCW7e4sTAZz1mC5U22vp8mAGuFdMXx", 8192l, "0.01 SYS","0.01 SYS", 0l);
			System.out.println("创建成功 = " + t2.getTransactionId()+" \n ");
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("============= 创建账户不抵押 ===============");
		try {	
			Transaction t3 = rpc.createAccount("5KQwrPbwdL6PhXujxW37FSSQZ1JiwsST4cqQzDeyXtP79zkvFD3","eosio","bbbb..54321", "EOS8eAX54cJtAngV2V22WZhRCW7e4sTAZz1mC5U22vp8mAGuFdMXx","EOS8eAX54cJtAngV2V22WZhRCW7e4sTAZz1mC5U22vp8mAGuFdMXx", 8192l);
			System.out.println("创建成功 = " + t3.getTransactionId()+" \n ");
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		System.out.println("============= 代理投票 ===============");
		try {
			List<String> produces = new ArrayList<>();
			produces.add("pppppeeeeooo");
			produces.add("mdddssssddds");
			produces.add("mdjddjddddds");
			rpc.voteproducer("5KQwrPbwdL6PhXujxW37FSSQZ1JiwsST4cqQzDeyXtP79zkvFD3", "epskdkdsddss","iuewjdkslsdc",produces);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("============= 关闭余额为0的token ===============");
		try {
			rpc.close("5KQwrPbwdL6PhXujxW37FSSQZ1JiwsST4cqQzDeyXtP79zkvFD3", "合约账户", "关闭账户", "0.0000 IPOS");
		}catch(ApiException e) {
			e.printStackTrace();
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("\n******************* Rpc End *******************");
	}
}