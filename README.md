# Firecracker (service d'impression)
Lancement : ./gradlew quarkusdev <br>
(port 8099)

## Rôle du service
Informer myGreffe d'une demande d'impression
1) Recevoir une demande d'impression (de Rhino)
2) Faire la demande d'impression auprès de myGreffe (appel direct en REST)
3) Recevoir une réponse asynchrone de myGreffe vua Kafka avec l'état de l'impression 
4) Réponse à la demande d'impression initiale de Rhino 

## Topics utilisés
impression_demande<br>
mygreffe_impression_reponse<br>
impression_reponse<br>

## Exemples de messages 

Sur les topics <strong>impression_demande</strong> et <strong>impression_reponse</strong>:
<p>
``
{
  "entete": {
    "idUnique": "659039e688c23ff08b4f905be07294ab66d600d4",
    "idLot": "12345",
    "dateHeureDemande": "2020-08-25T09:08:07",
    "idEmetteur": "L20057",
    "idReference": "zerealref123",
    "idGreffe": "0101",
    "typeDemande": "IMPRESSION"
  },
  "objetMetier": {
    "IDsortieDocument" : "IDSortieDocumentBidon",
    "urlFichierAImprimer": "http://toto.fr/popo",
    "rectoVerso": true,
    "nomImprimante" : "Canon-accueil",
    "nomBacEntree" : "marianne-RV",
    "nbExemplaires" : 1
  },
  "reponse": {
    "estReponseOk": false,
    "messageErreur": "noerror",
    "stackTrace": "nostracktrace"
  }
}
</p>
Sur le topic <strong>mygreffe_impression_reponse</strong> :<p>
``
{
  "entete": {
    "idUnique": "659039e688c23ff08b4f905be07294ab66d600d4",
    "idLot": "12345",
    "dateHeureDemande": "2020-08-25T09:08:07",
    "idEmetteur": "L20057",
    "idReference": "zerealref123",
    "idGreffe": "0101",
    "typeDemande": "IMPRESSION"
  },
  "objetMetier": {
    "urlFichierAImprimer": "http://toto.fr/popo",
    "messageRetour" : "OK"
  },
  "reponse": {
    "estReponseOk": true,
    "messageErreur": "noerror",
    "stackTrace": "nostracktrace"
  }
}
</p>