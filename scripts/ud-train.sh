#!/bin/bash

# Facilitates training OpenNLP models on Universal Dependencies (UD) data.

UD_PATH="./"
MODEL_PATH="./us-models"

SUBSET="EWT"
SUBSET_LC=`echo $SUBSET | tr '[:upper:]' '[:lower:]'`
LANG="English"
LANG_CODE="en"

# Tokenizer model
echo -e "\nTraining tokenizer model $SUBSET $LANG"
./apache-opennlp-1.9.3/bin/opennlp TokenizerTrainer.conllu -model ./ud-models/en-ud-$SUBSET_LC-tokens.bin -lang $LANG_CODE -data ./ud-treebanks-v2.7/UD_$LANG-$SUBSET/en_$SUBSET_LC-ud-train.conllu -encoding UTF-8
echo -e "\nEvaluating tokenizer model $SUBSET $LANG"
./apache-opennlp-1.9.3/bin/opennlp TokenizerMEEvaluator.conllu -model ./ud-models/en-ud-$SUBSET_LC-tokens.bin -data ./ud-treebanks-v2.7/UD_$LANG-$SUBSET/en_$SUBSET_LC-ud-test.conllu -encoding UTF-8

# Sentence model
echo -e "\nTraining sentence model $SUBSET $LANG"
./apache-opennlp-1.9.3/bin/opennlp SentenceDetectorTrainer.conllu -model ./ud-models/en-ud-$SUBSET_LC-sentence.bin -lang $LANG_CODE -data ./ud-treebanks-v2.7/UD_$LANG-$SUBSET/en_$SUBSET_LC-ud-train.conllu -encoding UTF-8 -sentencesPerSample 10
echo -e "Evaluating sentence model $SUBSET $LANG"
./apache-opennlp-1.9.3/bin/opennlp SentenceDetectorEvaluator.conllu -model ./ud-models/en-ud-$SUBSET_LC-sentence.bin -data ./ud-treebanks-v2.7/UD_$LANG-$SUBSET/en_$SUBSET_LC-ud-test.conllu -encoding UTF-8 -sentencesPerSample 10

# POS model
echo -e "\nTraining POS model $SUBSET $LANG"
./apache-opennlp-1.9.3/bin/opennlp POSTaggerTrainer.conllu -model ./ud-models/en-ud-$SUBSET_LC-pos.bin -data ./ud-treebanks-v2.7/UD_$LANG-$SUBSET/en_$SUBSET_LC-ud-train.conllu -encoding UTF-8 -lang $LANG_CODE
echo -e "\nEvaluating POS model $SUBSET $LANG"
./apache-opennlp-1.9.3/bin/opennlp POSTaggerEvaluator.conllu -model ./ud-models/en-ud-$SUBSET_LC-pos.bin -data ./ud-treebanks-v2.7/UD_$LANG-$SUBSET/en_$SUBSET_LC-ud-test.conllu -encoding UTF-8

