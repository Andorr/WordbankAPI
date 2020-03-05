# Wordbank API
![build](https://github.com/andorr/WordbankAPI/workflows/build/badge.svg)

### Functional requirements
- [x] Should be able to handle authentication (email, password)
- [ ] Should manage words and translations
    - [ ] Should include the type of word
    - [ ] Should be possible to group words into folders
- [ ] Support events
- [ ] Should handle quiz
    - [ ] Should analyze and give words that the user is bad at
- [ ] Should manage word of the day
- [ ] Should handle diaries/journals
    - [ ] Should be able to tag words with translations
    - [ ] Should be able to check if a certain word is used
- [ ] Should handle notes
    
#### Stretch goals
- [ ] Integrate with Naver Dictionary (https://developers.naver.com/docs/search/encyclopedia/)

### Words and translations
##### Folder
```json
{
  "id": "<ID>",
  "name": "folderName",
  "folders": ["id_1", "id_2"],
  "words": ["id_1", "id_2", "id_3"],
  "tags": ["TTMIK", "Color", "Verbs"]
}
```
##### Word
```json
{
    "id": "<ID>",
    "word": "한국어 단어",
    "type": "Verb",
    "tags": ["Electronics", "Color"],
    "translations": [
      "Korean word",
      "Korean vocab"
    ] 
}
```