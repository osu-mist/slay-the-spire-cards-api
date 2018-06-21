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
        def noResult
        Optional.with {
            noResult = resource.getCards(["attack"], absent(),
                    ["colorless"], ["rare"], absent(), absent(),
                    null, absent(), absent())
        }
        validateResponse(noResult, 200, null, null)
        assert noResult.entity.data == []

        // Test: Invalid type
        def invalidType
        invalidType = Optional.with {
            resource.getCards(["invalidType"], absent(),
                    null, null, absent(), absent(),
                    null, absent(), absent())
        }
        validateResponse(invalidType, 400, 1400, "Invalid types")

        // Test: Invalid color
        def invalidColor
        invalidColor = Optional.with {
            resource.getCards(null, absent(),
                    ["invalidColor"], null, absent(), absent(),
                    null, absent(), absent())
        }
        validateResponse(invalidColor, 400, 1400, "Invalid colors")

        // Test: Invalid rarity
        def invalidRarity
        invalidRarity = Optional.with {
            resource.getCards(null, absent(),
                    null, ["invalidRarity"], absent(), absent(),
                    null, absent(), absent())
        }
        validateResponse(invalidRarity, 400, 1400, "Invalid rarities")

        // Test: Invalid name
        def invalidName
        invalidName = Optional.with {
            resource.getCards(null, of("invalidname()"),
                    null, null, absent(), absent(),
                    null, absent(), absent())
        }
        validateResponse(invalidName, 400, 1400, "Invalid name")

        // Test: Invalid keyword
        def invalidKeyword
        invalidKeyword = Optional.with {
            resource.getCards(null, absent(),
                    null, null, absent(), absent(),
                    ["invalidKeyword()"], absent(), absent())
        }
        validateResponse(invalidKeyword, 400, 1400, "Invalid keywords")

        // Test: Out of range energy
        def outOfRangeEnergy
        outOfRangeEnergy = Optional.with {
            resource.getCards(null, absent(),
                    null, null, of(-1), of(1000),
                    null, absent(), absent())
        }
        validateResponse(outOfRangeEnergy, 400, 1400,
                "energyMin or energyMax out of range")

        // Test: energyMin XOR energyMax
        def energyXor
        energyXor = Optional.with {
            resource.getCards(null, absent(),
                    null, null, of(5), absent(),
                    null, absent(), absent())
        }
        validateResponse(energyXor, 400, 1400,
                "energyMin and energyMax must both be valid")

        // Test: energyMin > energyMax
        def wrongEnergyOrder
        wrongEnergyOrder = Optional.with {
            resource.getCards(null, absent(),
                    null, null, of(20), of(10),
                    null, absent(), absent())
        }
        validateResponse(wrongEnergyOrder, 400, 1400,
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
        validateResponse(validId, 200, null, null)
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
        validateResponse(idNotFound, 404, 1404, null)
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
        validateResponse(validPost, 201, null, null)

        // Test: invalid type
        resultObject.data.attributes.type = "badType"
        def invalidType = resource.postCard(resultObject)
        validateResponse(invalidType, 400, 1400, "Invalid type")
        resultObject.data.attributes.type = "skill"

        // Test: invalid color
        resultObject.data.attributes.color = "badColor"
        def invalidColor = resource.postCard(resultObject)
        validateResponse(invalidColor, 400, 1400, "Invalid color")
        resultObject.data.attributes.color = "red"

        // Test: invalid rarity
        resultObject.data.attributes.rarity = "badRarity"
        def invalidRarity = resource.postCard(resultObject)
        validateResponse(invalidRarity, 400, 1400, "Invalid rarity")
        resultObject.data.attributes.rarity = "basic"

        // Test: invalid name
        resultObject.data.attributes.name = "badName()"
        def invalidName = resource.postCard(resultObject)
        validateResponse(invalidName, 400, 1400, "Invalid name")
        resultObject.data.attributes.name = "Defend"

        // Test: invalid description
        resultObject.data.attributes.description = "badDescription()"
        def invalidDescription = resource.postCard(resultObject)
        validateResponse(invalidDescription, 400, 1400, "Invalid description")
        resultObject.data.attributes.description = "Gain 5 block."

        // Test: invalid energy
        resultObject.data.attributes.energy = 1000
        def invalidEnergy = resource.postCard(resultObject)
        validateResponse(invalidEnergy, 400, 1400, "Invalid energy")
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
        validateResponse(validPut, 200, null, null)

        // Test: invalid type
        resultObject.data.attributes.type = "badType"
        def invalidType = resource.putCard(id, resultObject)
        validateResponse(invalidType, 400, 1400, "Invalid type")
        resultObject.data.attributes.type = "skill"

        // Test: invalid color
        resultObject.data.attributes.color = "badColor"
        def invalidColor = resource.putCard(id, resultObject)
        validateResponse(invalidColor, 400, 1400, "Invalid color")
        resultObject.data.attributes.color = "red"

        // Test: invalid rarity
        resultObject.data.attributes.rarity = "badRarity"
        def invalidRarity = resource.putCard(id, resultObject)
        validateResponse(invalidRarity, 400, 1400, "Invalid rarity")
        resultObject.data.attributes.rarity = "basic"

        // Test: invalid name
        resultObject.data.attributes.name = "badName()"
        def invalidName = resource.putCard(id, resultObject)
        validateResponse(invalidName, 400, 1400, "Invalid name")
        resultObject.data.attributes.name = "Defend"

        // Test: invalid description
        resultObject.data.attributes.description = "badDescription()"
        def invalidDescription = resource.putCard(id, resultObject)
        validateResponse(invalidDescription, 400, 1400, "Invalid description")
        resultObject.data.attributes.description = "Gain 5 block."

        // Test: invalid energy
        resultObject.data.attributes.energy = 1000
        def invalidEnergy = resource.putCard(id, resultObject)
        validateResponse(invalidEnergy, 400, 1400, "Invalid energy")
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
        validateResponse(notFoundPut, 404, 1404, null)
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
        validateResponse(validDelete, 204, null, null)
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
        validateResponse(validDelete, 404, 1404, null)
    }

    static void validateResponse(def response, Integer status, Integer code,
                          String message) {
        if(status) {
            assert status == response.status
        }
        if(code) {
            assert code == response.entity.code
        }
        if(message) {
            assert response.entity.developerMessage.contains(message)
        }
    }
}
