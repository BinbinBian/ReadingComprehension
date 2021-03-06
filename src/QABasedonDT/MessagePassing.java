package QABasedonDT;

import javax.sound.midi.Instrument;

import FeatureExtraction.FeatureExtract_DT;

public class MessagePassing {
	/*************************
	 * 属性
	 */
	Word_Node root;
	Word_Node[] word_nodes;
	String question;
	String answer;
	String[] story;
	Model model;
	FeatureExtract_DT fx;

	/*********************************
	 * 方法
	 * 
	 * @param path
	 * @param count
	 */
	public MessagePassing(FeatureExtract_DT fx) {
		this.fx = fx;
	}

	// public MessagePassing(Word_Node[] word_Nodes,Model model,String
	// question,String[] story){
	// this.word_nodes = word_Nodes;
	// this.model = model;
	// this.question = question;
	// this.story = story;
	// for(int i = 0; i < word_Nodes.length;i++){
	// word_nodes[i].node_socre = new
	// double[word_nodes[i].support_sentense.size()];
	// for(int j = 0; j < word_nodes[i].node_socre.length;j++){
	// word_nodes[i].node_socre[j] = 0;
	// }
	// word_nodes[i].edge_feature = new
	// double[word_nodes[i].support_sentense.size()][18];
	// word_nodes[i].node_feature = new
	// double[word_nodes[i].support_sentense.size()][7];
	// }
	// for(int i = 0;i < word_Nodes.length;i++){
	// word_nodes[i].record_index = new
	// int[word_nodes[i].support_sentense.size()][word_nodes[i].childlist.size()];
	// }
	// }

	public void setInfo(Word_Node[] word_Nodes, Model model, String question, String answer, String[] story) {
		this.word_nodes = word_Nodes;
		this.model = model;
		this.question = question;
		this.answer = answer;
		this.story = story;
		for (int i = 0; i < word_Nodes.length; i++) {
			word_nodes[i].node_socre = new double[word_nodes[i].support_sentense.size()];
			for (int j = 0; j < word_nodes[i].node_socre.length; j++) {
				word_nodes[i].node_socre[j] = 0;
			}
			word_nodes[i].edge_feature = new double[word_nodes[i].support_sentense.size()][Model.edge_feature_num];
			word_nodes[i].node_feature = new double[word_nodes[i].support_sentense.size()][Model.node_feature_num];
			word_nodes[i].answer_feature = new double[word_nodes[i].support_sentense.size()][Model.global_feature_num];
		}
		for (int i = 0; i < word_Nodes.length; i++) {
			word_nodes[i].record_index = new int[word_nodes[i].support_sentense.size()][word_nodes[i].childlist.size()];
		}
	}

	public Word_Node findRoot() {
		Word_Node root = null;
		for (Word_Node word_Node : word_nodes) {
			if (word_Node.parent == null) {
				root = word_Node;
				break;
			}
		}
		return root;
	}

	public Object[] messagePassing() {
		root = findRoot();
		for (int i = 0; i < root.childlist.size(); i++) {
			collect(root, root.childlist.get(i), i);
		}
		root_collect();
		double max = 0;
		int index = 0;
		for (int i = 0; i < root.node_socre.length; i++) {
			if (root.node_socre[i] > max) {
				max = root.node_socre[i];
				index = i;
			}
		}
		getBackPath(root, index);
		double[] edge_feature = root.edge_feature[index];
		double[] node_feature = root.node_feature[index];
		edge_feature = vector_normalize(edge_feature);
		node_feature = vector_normalize(node_feature);
		Object[] ob = { max, edge_feature, node_feature };
		return ob;
	}

	public double[] vector_normalize(double[] vector_feature) {
		double count = 0;
		for (int i = 0; i < word_nodes.length; i++) {
			if (word_nodes[i].childlist.size() >= 2) {
				count++;
			}
		}
		if (count != 0) {
			for (int i = 0; i < vector_feature.length; i++) {
				vector_feature[i] = vector_feature[i] / count;
			}
		}
		return vector_feature;
	}

	// public Object[] messagePassing(){
	// root = findRoot();
	// for (int i = 0; i < root.childlist.size(); i++) {
	// collect(root, root.childlist.get(i), i);
	// }
	// root_collect();
	// double[] edge_feature = new double[Model.edge_feature_num];
	// double[] node_feature = new double[Model.node_feature_num];
	//// double[] answer_feature = new double[Model.answer_feature_num];
	// for(int i = 0;i < root.support_sentense.size();i++){
	// edge_feature = addFeature(edge_feature,root.edge_feature[i]);
	// node_feature = addFeature(node_feature,root.node_feature[i]);
	//// answer_feature = addFeature(answer_feature,root.answer_feature[i]);
	// }
	//// Object[] ob = { edge_feature, node_feature ,answer_feature};
	// Object[] ob = {edge_feature,node_feature};
	// return ob;
	// }

	public double[] addFeature(double[] vec1, double[] vec2) {
		for (int i = 0; i < vec1.length; i++) {
			vec1[i] = vec1[i] + vec2[i];
		}
		return vec1;
	}

	public void root_collect() {
		for (int i = 0; i < root.support_sentense.size(); i++) {
			root.currentSentense = root.support_sentense.get(i);
			double[] node_feature = fx.getNodeFeature(root, question, answer, story);
			// double[] answer_feature = fx.getAnswerFeature(root, answer,
			// question, story);
			double score_z = model.getNodeScore(node_feature);
			// double score_a = model.getAnswerScore(answer_feature);
			// root.node_socre[i] += score_z+score_a;
			root.node_socre[i] += score_z;
		}
	}

	// public void root_collect() {
	// for (int i = 0; i < root.support_sentense.size(); i++) {
	// root.currentSentense = root.support_sentense.get(i);
	// double[] node_feature = fx.getNodeFeature(root, question, story);
	// double score = model.getNodeScore(node_feature);
	// root.node_socre[i] += score;
	// }
	// }

	public void getBackPath(Word_Node word_Node, int index) {
		try {
			word_Node.currentSentense = word_Node.support_sentense.get(index);
			// System.out.println(index);
		} catch (Exception e) {
			// TODO: handle exception
		}
		for (int i = 0; i < word_Node.childlist.size(); i++) {
			getBackPath(word_Node.childlist.get(i), word_Node.record_index[index][i]);
		}
	}

	public void collect(Word_Node parent_node, Word_Node child_node, int index) {
		for (int i = 0; i < child_node.childlist.size(); i++) {
			collect(child_node, child_node.childlist.get(i), i);
		}
		if (!child_node.support_sentense.get(0).content.equals("*") || child_node.childlist.size() != 0) {
			sendMaxMessage(child_node, parent_node, index);
			// sendSumMessage(child_node, parent_node, index);
		}
	}

	public void sendSumMessage(Word_Node child_node, Word_Node parent_node, int index) {
		for (int i = 0; i < parent_node.support_sentense.size(); i++) {
			double[] edge_feature = null;
			double[] node_feature = null;
			// double[] answer_feature = null;
			parent_node.currentSentense = parent_node.support_sentense.get(i);
			for (int j = 0; j < child_node.support_sentense.size(); j++) {
				child_node.currentSentense = child_node.support_sentense.get(j);
				Object[] objects = getFeatureScore(child_node, parent_node);
				edge_feature = (double[]) objects[0];
				node_feature = (double[]) objects[1];
				// answer_feature = (double[]) objects[2];
				parent_node.add_EdgeFeautre(i, child_node.edge_feature[j], edge_feature);
				parent_node.add_NodeFeautre(i, child_node.node_feature[j], node_feature);
				// parent_node.add_AnswerFeature(i,
				// child_node.answer_feature[j], answer_feature);
			}
		}
	}

	public void sendMaxMessage(Word_Node child_node, Word_Node parent_node, int index) {
		for (int i = 0; i < parent_node.support_sentense.size(); i++) {
			double max = -Double.MAX_VALUE;
			int max_child = 0;
			double[] edge_feature = null;
			double[] node_feature = null;
			parent_node.currentSentense = parent_node.support_sentense.get(i);
			for (int j = 0; j < child_node.support_sentense.size(); j++) {
				child_node.currentSentense = child_node.support_sentense.get(j);
				Object[] objects = getFeatureScore(child_node, parent_node);
				double score = (double) objects[0];
				if (score >= max) {
					max = score;
					max_child = j;
					edge_feature = (double[]) objects[1];
					node_feature = (double[]) objects[2];
				}
			}
			parent_node.node_socre[i] += child_node.node_socre[max_child] + max;
			parent_node.add_EdgeFeautre(i, child_node.edge_feature[max_child], edge_feature);
			parent_node.add_NodeFeautre(i, child_node.node_feature[max_child], node_feature);
			parent_node.record_index[i][index] = max_child;
		}
	}

	// public Object[] getFeatureScore(Word_Node child_node, Word_Node
	// parent_node) {
	// double[] edge_feature = fx.getEdgeFeature(child_node, parent_node,
	// question, answer,story);
	// double[] node_feature = fx.getNodeFeature(child_node, question,
	// answer,story);
	//// double[] answer_feature = fx.getAnswerFeature(child_node, answer,
	// question, story);
	//// Object[] objects = { edge_feature, node_feature,answer_feature};
	// Object[] objects = {edge_feature,node_feature};
	// return objects;
	// }

	public Object[] getFeatureScore(Word_Node child_node, Word_Node parent_node) {
		double score = 0;
		double[] edge_feature = fx.getEdgeFeature(child_node, parent_node, question, answer, story);
		double[] node_feature = fx.getNodeFeature(child_node, question, answer, story);
		score += model.getEdgeScore(edge_feature);
		score += model.getNodeScore(node_feature);
		Object[] objects = { score, edge_feature, node_feature };
		return objects;
	}

}
