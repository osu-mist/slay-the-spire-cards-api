package edu.oregonstate.mist.api

import com.google.common.base.Optional
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.cardsapi.core.Card
import edu.oregonstate.mist.cardsapi.db.CardDAO
import edu.oregonstate.mist.cardsapi.db.CardFluent
import edu.oregonstate.mist.cardsapi.resources.CardsResource
import groovy.mock.interceptor.StubFor
import io.dropwizard.jersey.params.IntParam
import org.junit.Test

class CardsResourceTest {

    // Test CardsResource.getCards()
    @Test
    void testGetCards() {
        def mockDao = new StubFor(CardDAO)
        def mockFluent = new StubFor(CardFluent)
        mockDao.demand.getValidTypes ()
                { -> ["skill", "attack", "power", "status", "curse"] }
        mockDao.demand.getValidColors ()
                { -> ["red", "green", "blue", "colorless"] }
        mockDao.demand.getValidRarities ()
                { -> ["basic", "common", "uncommon", "rare"] }
        def dao = mockDao.proxyInstance()
        mockFluent.demand.getCards (0..10) {
            List<String> types, String name, List<String> colors,
            List<String> rarities, Integer energyMin, Integer energyMax,
            List<String> keywords, Integer cardNumber, Boolean isRandom -> []
        }
        def fluent = mockFluent.proxyInstance()
        CardsResource resource = new CardsResource(dao, null, fluent,
                dao.getValidTypes(), dao.getValidColors(), dao.getValidRarities())

        // Test: no result
        def noResult = resource.getCards(["attack"], Optional.absent(),
                ["colorless"], ["rare"], Optional.absent(), Optional.absent(),
                null, Optional.absent(), Optional.absent())
        assert noResult.status == 200
        assert noResult.entity.data == []

        // Test: Invalid type
        def invalidType = resource.getCards(["invalidType"], Optional.absent(),
                null, null, Optional.absent(), Optional.absent(),
                null, Optional.absent(), Optional.absent())
        assert invalidType.status == 400
        assert invalidType.entity.code == 1400
        assert invalidType.entity.developerMessage.contains("Invalid types")

        // Test: Invalid color
        def invalidColor = resource.getCards(null, Optional.absent(),
                ["invalidColor"], null, Optional.absent(), Optional.absent(),
                null, Optional.absent(), Optional.absent())
        assert invalidColor.status == 400
        assert invalidColor.entity.code == 1400
        assert invalidColor.entity.developerMessage.contains("Invalid colors")

        // Test: Invalid rarity
        def invalidRarity = resource.getCards(null, Optional.absent(),
                null, ["invalidRarity"], Optional.absent(), Optional.absent(),
                null, Optional.absent(), Optional.absent())
        assert invalidRarity.status == 400
        assert invalidRarity.entity.code == 1400
        assert invalidRarity.entity.developerMessage.contains("Invalid rarities")

        // Test: Invalid name
        def invalidName = resource.getCards(null, Optional.of("invalidname()"),
                null, null, Optional.absent(), Optional.absent(),
                null, Optional.absent(), Optional.absent())
        assert invalidName.status == 400
        assert invalidName.entity.code == 1400
        assert invalidName.entity.developerMessage.contains("Invalid name")

        // Test: Invalid keyword
        def invalidKeyword = resource.getCards(null, Optional.absent(),
                null, null, Optional.absent(), Optional.absent(),
                ["invalidKeyword()"], Optional.absent(), Optional.absent())
        assert invalidKeyword.status == 400
        assert invalidKeyword.entity.code == 1400
        assert invalidKeyword.entity.developerMessage.contains("Invalid keywords")

        // Test: Out of range energy
        def outOfRangeEnergy = resource.getCards(null, Optional.absent(),
                null, null, Optional.of(-1), Optional.of(1000),
                null, Optional.absent(), Optional.absent())
        assert outOfRangeEnergy.status == 400
        assert outOfRangeEnergy.entity.code == 1400
        assert outOfRangeEnergy.entity.developerMessage.contains(
                "energyMin or energyMax out of range")

        // Test: energyMin XOR energyMax
        def energyXor = resource.getCards(null, Optional.absent(),
                null, null, Optional.of(5), Optional.absent(),
                null, Optional.absent(), Optional.absent())
        assert energyXor.status == 400
        assert energyXor.entity.code == 1400
        assert energyXor.entity.developerMessage.contains(
                "energyMin and energyMax must both be valid")

        // Test: energyMin > energyMax
        def wrongEnergyOrder = resource.getCards(null, Optional.absent(),
                null, null, Optional.of(20), Optional.of(10),
                null, Optional.absent(), Optional.absent())
        assert wrongEnergyOrder.status == 400
        assert wrongEnergyOrder.entity.code == 1400
        assert wrongEnergyOrder.entity.developerMessage.contains(
                "energyMin must not be greater than energyMax")
    }

    // Test: CardsResource.getCardById

    // Test: valid id
    @Test
    void testValidId() {
        Card card = new Card()
        def mockDao = new StubFor(CardDAO)
        mockDao.demand.getCardById ()
                { Integer i -> card }
        def dao = mockDao.proxyInstance()
        CardsResource resource = new CardsResource(dao, null, null,
                null, null, null)

        IntParam id = new IntParam("1")
        def validId = resource.getCardById(id)
        assert validId.status == 200
    }

    // Test: id not found
    @Test
    void testNotFound() {
        def mockDao = new StubFor(CardDAO)
        mockDao.demand.getCardById ()
                { Integer i -> null }
        def dao = mockDao.proxyInstance()
        CardsResource resource = new CardsResource(dao, null, null,
                null, null, null)

        IntParam id = new IntParam("1")
        def idNotFound = resource.getCardById(id)
        assert idNotFound.status == 404
    }

    // Test: CardsResource.postCard
    @Test
    void testPostCard() {
        Card card = new Card(
                type: "skill",
                name: "Defend",
                color: "red",
                rarity: "basic",
                energy: 1,
                description: "Gain 5 block.")

        ResultObject resultObject = new ResultObject(
                links: {},
                data: [id: 1,
                       type: "card",
                       attributes: card,
                       links: null])

        def mockDao = new StubFor(CardDAO)
        mockDao.demand.getValidTypes ()
                { -> ["skill", "attack", "power", "status", "curse"] }
        mockDao.demand.getValidColors ()
                { -> ["red", "green", "blue", "colorless"] }
        mockDao.demand.getValidRarities ()
                { -> ["basic", "common", "uncommon", "rare"] }
        mockDao.demand.getCardById ()
                { Integer i -> card }
        mockDao.demand.postCard ()
                { Integer i, Card c -> card }
        mockDao.demand.getNextId ()
                { -> 1 }
        def dao = mockDao.proxyInstance()
        CardsResource resource = new CardsResource(dao, null, null,
                dao.getValidTypes(), dao.getValidColors(), dao.getValidRarities())

        // Test: post a card
        def validPost = resource.postCard(resultObject)
        assert validPost.status == 201

        // Test: invalid type
        resultObject.data.attributes.type = "badType"
        def invalidType = resource.postCard(resultObject)
        assert invalidType.status == 400
        assert invalidType.entity.developerMessage.contains("Invalid type")
        resultObject.data.attributes.type = "skill"

        // Test: invalid color
        resultObject.data.attributes.color = "badColor"
        def invalidColor = resource.postCard(resultObject)
        assert invalidColor.status == 400
        assert invalidColor.entity.developerMessage.contains("Invalid color")
        resultObject.data.attributes.color = "red"

        // Test: invalid rarity
        resultObject.data.attributes.rarity = "badRarity"
        def invalidRarity = resource.postCard(resultObject)
        assert invalidRarity.status == 400
        assert invalidRarity.entity.developerMessage.contains("Invalid rarity")
        resultObject.data.attributes.rarity = "basic"

        // Test: invalid name
        resultObject.data.attributes.name = "badName()"
        def invalidName = resource.postCard(resultObject)
        assert invalidName.status == 400
        assert invalidName.entity.developerMessage.contains("Invalid name")
        resultObject.data.attributes.name = "Defend"

        // Test: invalid description
        resultObject.data.attributes.description = "badDescription()"
        def invalidDescription = resource.postCard(resultObject)
        assert invalidDescription.status == 400
        assert invalidDescription.entity.developerMessage.contains("Invalid description")
        resultObject.data.attributes.description = "Gain 5 block."

        // Test: invalid energy
        resultObject.data.attributes.energy = 1000
        def invalidEnergy = resource.postCard(resultObject)
        assert invalidEnergy.status == 400
        assert invalidEnergy.entity.developerMessage.contains("Invalid energy")
        resultObject.data.attributes.energy = 1
    }

    // Test: CardsResource.putCard

    // Test: valid id of card
    @Test
    void testValidPutCard() {
        Card card = new Card(
                type: "skill",
                name: "Defend",
                color: "red",
                rarity: "basic",
                energy: 1,
                description: "Gain 5 block.")

        ResultObject resultObject = new ResultObject(
                links: {},
                data: [id: 1,
                       type: "card",
                       attributes: card,
                       links: null])

        def mockDao = new StubFor(CardDAO)
        mockDao.demand.getValidTypes ()
                { -> ["skill", "attack", "power", "status", "curse"] }
        mockDao.demand.getValidColors ()
                { -> ["red", "green", "blue", "colorless"] }
        mockDao.demand.getValidRarities ()
                { -> ["basic", "common", "uncommon", "rare"] }
        mockDao.demand.getCardById ()
                { Integer i -> card }
        mockDao.demand.putCard ()
                { Integer i, Card c -> card }
        mockDao.demand.cardExists (0..7)
                { Integer i -> true }
        def dao = mockDao.proxyInstance()
        CardsResource resource = new CardsResource(dao, null, null,
                dao.getValidTypes(), dao.getValidColors(), dao.getValidRarities())
        IntParam id = new IntParam("1")

        // Test: put a card
        def validPut = resource.putCard(id, resultObject)
        assert validPut.status == 200

        // Test: invalid type
        resultObject.data.attributes.type = "badType"
        def invalidType = resource.putCard(id, resultObject)
        assert invalidType.status == 400
        assert invalidType.entity.developerMessage.contains("Invalid type")
        resultObject.data.attributes.type = "skill"

        // Test: invalid color
        resultObject.data.attributes.color = "badColor"
        def invalidColor = resource.putCard(id, resultObject)
        assert invalidColor.status == 400
        assert invalidColor.entity.developerMessage.contains("Invalid color")
        resultObject.data.attributes.color = "red"

        // Test: invalid rarity
        resultObject.data.attributes.rarity = "badRarity"
        def invalidRarity = resource.putCard(id, resultObject)
        assert invalidRarity.status == 400
        assert invalidRarity.entity.developerMessage.contains("Invalid rarity")
        resultObject.data.attributes.rarity = "basic"

        // Test: invalid name
        resultObject.data.attributes.name = "badName()"
        def invalidName = resource.putCard(id, resultObject)
        assert invalidName.status == 400
        assert invalidName.entity.developerMessage.contains("Invalid name")
        resultObject.data.attributes.name = "Defend"

        // Test: invalid description
        resultObject.data.attributes.description = "badDescription()"
        def invalidDescription = resource.putCard(id, resultObject)
        assert invalidDescription.status == 400
        assert invalidDescription.entity.developerMessage.contains("Invalid description")
        resultObject.data.attributes.description = "Gain 5 block."

        // Test: invalid energy
        resultObject.data.attributes.energy = 1000
        def invalidEnergy = resource.putCard(id, resultObject)
        assert invalidEnergy.status == 400
        assert invalidEnergy.entity.developerMessage.contains("Invalid energy")
        resultObject.data.attributes.energy = 1
    }

    // Test: id not found in put
    @Test
    void testNotFoundPutCard() {
        def mockDao = new StubFor(CardDAO)
        mockDao.demand.cardExists ()
                { Integer i -> false }
        def dao = mockDao.proxyInstance()
        CardsResource resource = new CardsResource(dao, null, null,
                null, null, null)
        IntParam id = new IntParam("1")

        // Test: put a card
        def notFoundPut = resource.putCard(id, null)
        assert notFoundPut.status == 404
    }

    // Test: CardsResource.deleteCard
    // Test: valid id
    @Test
    void testValidDelete() {
        def mockDao = new StubFor(CardDAO)
        mockDao.demand.cardExists ()
                { Integer i -> true }
        mockDao.demand.deleteCard ()
                { Integer i -> }
        def dao = mockDao.proxyInstance()
        CardsResource resource = new CardsResource(dao, null, null,
                null, null, null)
        IntParam id = new IntParam("1")

        def validDelete = resource.deleteCard(id)
        assert validDelete.status == 204
    }

    // Test: id not found in delete
    @Test
    void testNotFoundDelete() {
        def mockDao = new StubFor(CardDAO)
        mockDao.demand.cardExists ()
                { Integer i -> false }
        def dao = mockDao.proxyInstance()
        CardsResource resource = new CardsResource(dao, null, null,
                null, null, null)
        IntParam id = new IntParam("1")

        def validDelete = resource.deleteCard(id)
        assert validDelete.status == 404
    }
}
