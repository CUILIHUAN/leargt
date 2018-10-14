import java.util.*;
//import java.net.*;
/*
* Node���̃N���X
*/
public class NodeInstance {
	// �t�B�[���h
	public int HASH_MAX_VALUE; // �n�b�V����Ԃ̍ő�l(2�ׂ̂���)
	public long Node_ID; // Node�̃m�[�hID
	public long Original_Node_ID; // ��ʃ����O�ɓ���O�̌���ID
	public NodeInstance Former_Node; // �O�C�m�[�h���w���|�C���^
	public NodeInstance Successor_Node; // ��C�m�[�h���w���|�C���^
	private Vector<Long> fingerTable; // �t�B���K�[�e�[�u��
	private Vector manageNodes; // �Ǘ�����̈�̃m�[�h���L������z��
	private Vector<Cache> Cache;
	static final float TIMEEXTE = 10000;
	int total_re;
	
	/////�C�C�C�C�C�C �R���X�g���N�^ �C�C�C�C�C�C�C/////
	// �����̓n�b�V����Ԃ̃T�C�Y
	public NodeInstance(int HASH_MAX, long nodeid){
		this.HASH_MAX_VALUE = HASH_MAX;
		fingerTable = new Vector<Long>();
		Node_ID = nodeid;
		Cache = new Vector<Cache>();
	}
	
	
	/////�C�C�C�C�C�C ���\�b�h �C�C�C�C�C�C�C/////
	////////
	
	// Cache�̒ǉ��A�����A�폜���\�b�h
	public void add_cache(Cache c){
		
		this.Cache.addElement(c);
		
	}
	
	public boolean search_cache(long target){
		Cache c,c1;
		int j;
		long nodeid;
	//	System.out.println("search cache : target "+target+" in "+this.Node_ID);
		// cache_node������΁Atrue
		for(int i=0;i<this.Cache.size();i++){
			c = this.Cache.elementAt(i);
			nodeid = c.cache_id;
			if(nodeid == target){
					// c.time_limit =  TIMEEXTE + c.time_limit;
					return true;
			}
		}
		return false;
	}
	
	public boolean del_cache(double time){
		Cache c1;
		for(int i=0;i<this.Cache.size();i++){
			c1= this.Cache.elementAt(i);
			if(c1.time_limit<=time)
			{
				this.Cache.remove(i);
				return true;
				
			}
			
		}
		return false;
	}
	
	// �t�B���K�[�e�[�u���̍X�V// ID:IP���X�g����t�B���K�[�e�[�u���𐶐�
	public void refresh(Vector node_list) {
		this.fingerTable = (Vector<Long>)createFtable(node_list, Node_ID);
		Former_Node = getFormerNode(node_list, Node_ID);
		Successor_Node = getSuccessorNode(node_list, Node_ID);
		nodeJoin();
	}
	////////+++++ Proxy�V�K�Q������(�O��̃m�[�h�ɎQ�������m) +++++////////
	public void nodeJoin() {
		//        System.out.println("nodeRefresh");
		if(Successor_Node.Node_ID != Node_ID)
		Successor_Node.Former_Node = this;
		if(Former_Node.Node_ID != Node_ID)
		Former_Node.Successor_Node = this;
	}
	////////+++++ Proxy�E�ޏ���(�O�C�E��C�m�[�h�ɒE�ނ̎|�����m����) +++++////////
	public void nodeLeave() {
		System.out.println("�l�b�g���[�N����E�ނ��܂�");
		// ID:IP���X�g�ɒE�ނ��邱�Ƃ����m���ă��X�g���X�V����
		// ��C�m�[�h�ɂ́A������ID�ƑO�C�m�[�h��ID��IP��Y����
		Successor_Node.Former_Node = this.Former_Node;
		// �O�C�m�[�h�ɂ́A������ID�ƌ�C�m�[�h��ID��IP��Y����
		Former_Node.Successor_Node = this.Successor_Node;
	}
	
	////////+++++ node�T������ +++++////////
	// �t�B���K�[�e�[�u���𗘗p���Ď��ɃA�N�Z�X���ׂ�Proxy��IP�����߂�
	// �����͒T���m�[�h��ID, �Ԃ�l�͎��ɃX�e�[�^�X�R�[�h�ƁA�A�N�Z�X���ׂ��m�[�h��IP
	public boolean query(long target) {
		// �������Ǘ�����̈���ɂ���΁Atrue
		if(this.Former_Node.Node_ID < this.Node_ID){
			if ((target > this.Former_Node.Node_ID)
			&& (target <= this.Node_ID)){
				////                System.out.println("func query: true: nodeid = "+this.Node_ID+" target = "+target+" former = "+this.Former_Node.Node_ID);
				return true;
			}else{
				//                System.out.println("func query: false: nodeid = "+this.Node_ID+" target = "+target+" former = "+this.Former_Node.Node_ID);
				return false;
			}
		}else {
			if ((target > this.Former_Node.Node_ID)
			|| (target <= this.Node_ID)){
				////                System.out.println("func query: true: nodeid = "+this.Node_ID+" target = "+target+" former = "+this.Former_Node.Node_ID);
				return true;
			}else{
				//                System.out.println("func query: false: nodeid = "+this.Node_ID+" target = "+target+" former = "+this.Former_Node.Node_ID);
				return false;
			}
		}
	}
	
	////////+++++ Proxy�T������ +++++////////
	// �t�B���K�[�e�[�u���𗘗p���Ď��ɃA�N�Z�X���ׂ�Proxy��IP�����߂�
	// �����͒T���m�[�h��ID, �Ԃ�l�͎��ɃX�e�[�^�X�R�[�h�ƁA�A�N�Z�X���ׂ��m�[�h��IP
	public long nexthop(long target) {
		Long nodeid1, nodeid2;
		
		// �T���m�[�h�������ƌ�C�m�[�h�̊Ԃɂ���΁A��C�m�[�h
		/*        if(this.Successor_Node.Node_ID > this.Node_ID){
			if ((target <= this.Successor_Node.Node_ID)
			&& (target > this.Node_ID)){
				System.out.println("X:�m�[�h"+target+"�̒T�����m�[�h"+ this.Successor_Node.Node_ID+"�Ɉϑ����܂�");
				return this.Successor_Node.Node_ID;
			}
		}else if(this.Successor_Node.Node_ID < this.Node_ID){
			if ((target <= this.Former_Node.Node_ID)
			|| (target > this.Node_ID)){
				System.out.println("Y:�m�[�h"+target+"�̒T�����m�[�h"+ this.Successor_Node.Node_ID+"�Ɉϑ����܂�");
				return this.Successor_Node.Node_ID;
			}
		}
		*/
		nodeid1 = fingerTable.get(0);
		if(nodeid1.longValue() > this.Node_ID){
			if ((target <= nodeid1.longValue())
			&& (target > this.Node_ID)){
				//                System.out.println("X:�m�[�h"+target+"�̒T�����m�[�h"+ nodeid1.longValue()+"�Ɉϑ����܂�");
				return nodeid1.longValue();
			}
		}else if(nodeid1.longValue() < this.Node_ID){
			if ((target <= nodeid1.longValue())
			|| (target > this.Node_ID)){
				//                System.out.println("Y:�m�[�h"+target+"�̒T�����m�[�h"+ nodeid1.longValue()+"�Ɉϑ����܂�");
				return nodeid1.longValue();
			}
		}
		//        else{
			// �����Ȃ���΁A�t�B���K�[�E�e�[�u����"�J�nID"�G���g��������
			// ���߂�L�[�̑O�C�m�[�h�Ƃ��čł��߂����̂�T��
			for(int i=1; i<fingerTable.size(); i++){
				nodeid1 = fingerTable.get(i-1);
				nodeid2 = fingerTable.get(i);
				//                System.out.println(" nodeid1 = "+nodeid1.longValue()+" nodeid2 = "+nodeid2.longValue()+" target ="+target);
				if(nodeid1.longValue() <= nodeid2.longValue()){
					if ((target < nodeid2.longValue())
					&& (target >= nodeid1.longValue())){
						//                        System.out.println("A:�m�[�h"+target+"�̒T�����m�[�h"+nodeid1.longValue()+"�Ɉϑ����܂�");
						return nodeid1.longValue();
					}
				}else if(nodeid1.longValue() > nodeid2.longValue()){
					if ((target < nodeid2.longValue())
					|| (target >= nodeid1.longValue())){
						//                        System.out.println("B:�m�[�h"+target+"�̒T�����m�[�h"+nodeid1.longValue()+"�Ɉϑ����܂�");
						return nodeid1.longValue();
					}
				}
			}
			nodeid2 = fingerTable.get(fingerTable.size()-1);
			//            System.out.println("C:�m�[�h"+target+"�̒T�����m�[�h"+nodeid2.longValue()+"�Ɉϑ����܂�");
			return nodeid2.longValue();
		//        }
		//    return 0;
	}
	
	////////+++++ ���݂̃t�B���K�[�e�[�u�����ꗗ�\�� +++++////////
	public void showFinger() {
		System.out.println("++[ Finger Table ]++");
		System.out.println("My Node_ID = " + this.Node_ID+" former="+this.Former_Node.Node_ID+" successor="+this.Successor_Node.Node_ID);
		
		for (int i = 0; i < fingerTable.size(); i++) {
			Long nodeid = fingerTable.get(i);
			System.out.println(i+"�� :"+nodeid.longValue());
		}
	}
	
	
	// ���݂̃��[�J��ID:IP���X�g����A�t�B���K�[�e�[�u�������������BVector�^�ɒ����ĕԂ�
	public Vector<Long> createFtable(Vector node_list, long NodeID){
		int tableSize = 16; // �e�[�u���̃T�C�Y�̓n�b�V����Ԃ̃r�b�g�T�C�Y
		/* �ꂪ2�̑ΐ��v�Z��J2SE1.5�łȂ��Ǝg���Ȃ��̂ł����ł͎w�肷�� */
		NodeInstance succ;
		long nodeid;
		Vector<Long> data = new Vector<Long>();
		
		//        System.out.println("createFtable");
		
		for(int i = 0; i<node_list.size();i++){
			NodeInstance nd = (NodeInstance)node_list.elementAt(i);
			///            System.out.print(" "+ nd.Node_ID);
		}
		///        System.out.println();
		
		// Vector�̊e�v�f��1�����肵�A�i�[����
		for(int i = 0; i < tableSize; i++){
			// start�l�͂��ꂼ�ꎩ����ID����2�ׂ̂���l������������
			long start = (long)(NodeID + Math.pow(2,i));
			int flag = 0;
			for(int j = 0; j < node_list.size(); j++){
				NodeInstance nd = (NodeInstance)node_list.elementAt(j);
				if(start == nd.Node_ID)
				flag = 1;
			}
			if(flag == 0){
				succ = getSuccessorNode(node_list, start);
				Long sucNodeID = new Long(succ.Node_ID);
				data.addElement(sucNodeID);
			}else{
				Long sucNodeID = new Long(start);
				data.addElement(sucNodeID);
			}
		}
		
		for(int i = 0; i<node_list.size();i++){
			NodeInstance nd = (NodeInstance)node_list.elementAt(i);
			///            System.out.print(" "+ nd.Node_ID);
		}
		///        System.out.println();
		
		return data;
	}
	
	// ID:IP���X�g����w��n�b�V��ID�ɑ΂���O�C�m�[�h�����o���B�߂�l��Node�^��
	public NodeInstance getFormerNode(Vector node_list, long NodeID){
		// NodeID���n�b�V����Ԃ̍ő�l���z������A��]���Ƃ�
		//        System.out.println("getFormerNode");
		if(NodeID > HASH_MAX_VALUE)
		NodeID = NodeID % HASH_MAX_VALUE;
		
		NodeInstance nd = null;
		for(int i = node_list.size()-1;i>=0;i--){
			nd = (NodeInstance)node_list.elementAt(i);
			if(NodeID > nd.Node_ID){
				return nd;
			}
		}
		nd = (NodeInstance)node_list.lastElement();
		return nd;
	}
	// ID:IP���X�g����w��n�b�V��ID���Ǘ�����m�[�h(��C�m�[�h)�����o���B�߂�l��Node�^��
	public NodeInstance getSuccessorNode(Vector node_list, long NodeID){
		// NodeID���n�b�V����Ԃ̍ő�l���z������A��]���Ƃ�
		int i;
		
		//        System.out.println("getSuccessorNode");
		if(NodeID > HASH_MAX_VALUE)
		NodeID = NodeID % HASH_MAX_VALUE;
		
		NodeInstance referNode= (NodeInstance)node_list.elementAt(0);
		for(i=0; i<node_list.size(); i++){
			referNode = (NodeInstance)node_list.elementAt(i);
			// �w��ID���Q�ƒ��̃m�[�hID���傫��
			if(NodeID >= referNode.Node_ID)
			continue;
			else
			break;
		}
		if(i == node_list.size())
		referNode = (NodeInstance)node_list.elementAt(0);
		return referNode;
	}
}
