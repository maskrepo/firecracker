package fr.convergence.proddoc.reactive.fr.convergence.proddoc.kafka

import fr.convergence.proddoc.model.lib.obj.MaskMessage
import fr.convergence.proddoc.model.metier.DemandeImpression
import fr.convergence.proddoc.util.MyGreffeUtil
import fr.convergence.proddoc.util.WSUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.slf4j.LoggerFactory.getLogger
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.DefaultValue
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType.APPLICATION_JSON

@ApplicationScoped
class ImpressionDemande(@Inject var myGreffeUtil : MyGreffeUtil) {

    companion object {
        private val LOG = getLogger(ImpressionDemande::class.java)
    }

    /**
     * si un MaskMessage arrive sur le topic "IMPRESSION_DEMANDE" (Incoming) :
     * fait le "passe-plat" et appelle myGreffe pour lui demander d'imprimer le fichier
     * l'objet métier du message reçu est un "DemandeImpression"
     **/
    @Incoming("impression_demande")
    fun traiterEvenementImpressionDemande(messageIn: MaskMessage) {

        //@TODO ces requires sont à basculer dans le maskIOHadler
        requireNotNull(messageIn.entete.typeDemande) { "message.entete.typeDemande est null" }
        requireNotNull(messageIn.objetMetier) { "message.objectMetier est null" }

        //récupérer dans le message, les valeurs des paramètres à passer à myGreffe
        val objetMessage = messageIn.recupererObjetMetier<DemandeImpression>()
        val idSortieDocument = if (objetMessage.iDsortieDocument == null) 0 else objetMessage.iDsortieDocument
        val nbExemplaires = objetMessage.nbExemplaires
        val nomBacEntree = objetMessage.nomBacEntree
        val rectoVerso = objetMessage.rectoVerso
        val nomImprimante = objetMessage.nomImprimante
        val urlFichierAImprimer = objetMessage.urlFichierAImprimer

        LOG.debug("Demande d'impression reçue pour IDsortieDocument: $idSortieDocument et fichier $urlFichierAImprimer")


        // appeler URI de myGreffe avec les paramètres nécessaires
        GlobalScope.launch {
            val retourMyGreffe = myGreffeUtil.demandeRestURLmyGreffe(
                    pathDuService = "/impression/demandeDepuisProddoc",
                    parametresRequete = mapOf(
                            "idSortieDocument" to idSortieDocument,
                            "nbExemplaires" to nbExemplaires,
                            "nomBacEntree" to nomBacEntree,
                            "rectoVerso" to rectoVerso,
                            "nomImprimante" to nomImprimante,
                            "urlFichierAImprimer" to urlFichierAImprimer,
                            "idReference" to  messageIn.entete.idReference,
                            "idLot" to messageIn.entete.idLot,
                            "codeUtilisateur" to "proddoc"
                    ),
                    timeOut = 5000,
                    retourAttendu = APPLICATION_JSON)
                    .readEntity(String::class.java)
            LOG.info("Retour de myGreffe suite à l'appel REST d'impression : $retourMyGreffe")
        }
    }
}