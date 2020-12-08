#!/bin/bash

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

set -e

# This script facilitates training OpenNLP models on Universal Dependencies (UD) 2.7 data.

# Script configuration
UD_HOME="./"
OPENNLP_HOME="./apache-opennlp-1.9.3"
OUTPUT_MODELS="./ud-models"
EVAL_AFTER_TRAINING="true"
ENCODING="UTF-8"

# Model(s) to train
declare -a MODELS=("English|en|EWT" "Dutch|nl|Alpino" "French|fr|FTB" "German|de|GSD" "Italian|it|VIT")

# Create output directory
mkdir -p ${OUTPUT_MODELS}

for i in "${MODELS[@]}"
do

  echo $i
  LANG=`echo $i | cut -d'|' -f1`
  LANGCODE=`echo $i | cut -d'|' -f2`
  SUBSET=`echo $i | cut -d'|' -f3`
  SUBSETLC=`echo ${SUBSET} | tr '[:upper:]' '[:lower:]'`

  # Tokenizer model
  echo -e "\nTraining tokenizer model ${SUBSET} ${LANG}..."
  ${OPENNLP_HOME}/bin/opennlp TokenizerTrainer.conllu -model ${OUTPUT_MODELS}/${LANGCODE}-ud-${SUBSETLC}-tokens.bin -lang ${LANGCODE} -data ./ud-treebanks-v2.7/UD_${LANG}-${SUBSET}/${LANGCODE}_${SUBSETLC}-ud-train.conllu -encoding ${ENCODING} > ${OUTPUT_MODELS}/${LANGCODE}-ud-${SUBSETLC}-tokens.train

  if [ ${EVAL_AFTER_TRAINING} == "true" ]; then
    echo -e "\nEvaluating tokenizer model ${SUBSET} ${LANG}..."
    ${OPENNLP_HOME}/bin/opennlp TokenizerMEEvaluator.conllu -model ${OUTPUT_MODELS}/${LANGCODE}-ud-${SUBSETLC}-tokens.bin -data ./ud-treebanks-v2.7/UD_${LANG}-${SUBSET}/${LANGCODE}_${SUBSETLC}-ud-test.conllu -encoding ${ENCODING} > ${OUTPUT_MODELS}/${LANGCODE}-ud-${SUBSETLC}-tokens.eval
  fi

  # Sentence model
  echo -e "\nTraining sentence model ${SUBSET} ${LANG}..."
  ${OPENNLP_HOME}/bin/opennlp SentenceDetectorTrainer.conllu -model ${OUTPUT_MODELS}/${LANGCODE}-ud-${SUBSETLC}-sentence.bin -lang ${LANGCODE} -data ./ud-treebanks-v2.7/UD_${LANG}-${SUBSET}/${LANGCODE}_${SUBSETLC}-ud-train.conllu -encoding ${ENCODING} -sentencesPerSample 10 > ${OUTPUT_MODELS}/${LANGCODE}-ud-${SUBSETLC}-sentence.train

  if [ ${EVAL_AFTER_TRAINING} == "true" ]; then
    echo -e "Evaluating sentence model ${SUBSET} ${LANG}..."
    ${OPENNLP_HOME}/bin/opennlp SentenceDetectorEvaluator.conllu -model ${OUTPUT_MODELS}/${LANGCODE}-ud-${SUBSETLC}-sentence.bin -data ./ud-treebanks-v2.7/UD_${LANG}-${SUBSET}/${LANGCODE}_${SUBSETLC}-ud-test.conllu -encoding ${ENCODING} -sentencesPerSample 10 > ${OUTPUT_MODELS}/${LANGCODE}-ud-${SUBSETLC}-sentence.eval
  fi

  # POS model
  echo -e "\nTraining POS model ${SUBSET} ${LANG}..."
  ${OPENNLP_HOME}/bin/opennlp POSTaggerTrainer.conllu -model ${OUTPUT_MODELS}/${LANGCODE}-ud-${SUBSETLC}-pos.bin -data ./ud-treebanks-v2.7/UD_${LANG}-${SUBSET}/${LANGCODE}_${SUBSETLC}-ud-train.conllu -encoding ${ENCODING} -lang ${LANGCODE} > ${OUTPUT_MODELS}/${LANGCODE}-ud-${SUBSETLC}-pos.eval > ${OUTPUT_MODELS}/${LANGCODE}-ud-${SUBSETLC}-pos.train

  if [ ${EVAL_AFTER_TRAINING} == "true" ]; then
    echo -e "\nEvaluating POS model ${SUBSET} ${LANG}..."
    ${OPENNLP_HOME}/bin/opennlp POSTaggerEvaluator.conllu -model ${OUTPUT_MODELS}/${LANGCODE}-ud-${SUBSETLC}-pos.bin -data ./ud-treebanks-v2.7/UD_${LANG}-${SUBSET}/${LANGCODE}_${SUBSETLC}-ud-test.conllu -encoding ${ENCODING} > ${OUTPUT_MODELS}/${LANGCODE}-ud-${SUBSETLC}-pos.eval
  fi

done
