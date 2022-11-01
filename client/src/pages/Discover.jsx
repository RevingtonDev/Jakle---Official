import React, { Component } from "react";
import { createRef } from "react";
import { Helmet } from "react-helmet";
import { useNavigate, useSearchParams } from "react-router-dom";
import { discover_people } from "../api/request";
import { Search } from "../components/Images";
import { Pagination } from "../components/Pagination";
import { Profile } from "../components/Profile";

export const Discover = (props) => {
  let params = useSearchParams()[0];
  if (!params) params = new URLSearchParams();
  if (!params.get("page") || params.get("page") === "") params.set("page", "1");
  return (
    <DiscoverComponent
      {...props}
      page={parseInt(params.get("page"))}
      query={params.get("query")}
      params={params}
      navigate={useNavigate()}
    />
  );
};

class DiscoverComponent extends Component {
  constructor(props) {
    super(props);

    this.state = {
      isWaiting: false,
      pages: undefined,
      data: [],
    };

    this.searchBox = createRef();
    this.searchQuery = this.props.query;
    this.searchPage = this.props.page;
    this.changePage = this.changePage.bind(this);

    this.changeElementData = this.changeElementData.bind(this);
  }

  async componentDidMount() {
    if (this.searchQuery !== undefined || this.searchQuery !== "")
      this.retrieveData();
  }

  async componentDidUpdate() {
    if (
      this.searchQuery !== this.props.query ||
      this.searchPage !== this.props.page
    ) {
      this.searchQuery = this.props.query;
      this.searchPage = this.props.page;
      this.retrieveData();
    }
  }

  async retrieveData() {
    if (
      this.searchQuery !== undefined &&
      this.searchQuery !== "" &&
      this.searchPage !== undefined
    ) {
      this.setState({ isWaiting: true });
      let data = await discover_people(this.searchQuery, this.searchPage, 15);
      if (data) {
        this.setState({ pages: data.total_pages, data: data.results });
      }
      this.setState({ isWaiting: false });
    }
  }

  changeElementData(id, key, val) {
    this.state.data.forEach((elm) => {
      if (elm.id === id) {
        elm[key] = val;
      }
    });
  }

  search() {
    this.props.params.set("query", this.searchBox.current.value);
    this.props.params.set("page", 1);
    this.props.navigate({
      pathname: "./",
      search: this.props.params.toString(),
    });
  }

  changePage(page) {
    this.props.params.set("page", page);
    this.props.navigate({
      pathname: "./",
      search: this.props.params.toString(),
    });
  }

  render() {
    const { isWaiting, pages, data } = this.state;
    const { params, query } = this.props;
    return (
      <>
        <Helmet>
          <title>Jakle - Discover</title>
        </Helmet>
        <section className="page-container layout-page center justify-start column-flex">
          <div className="layout-title">Discover People</div>
          <div className="row center row-flex">
            <div className="data-input">
              <input
                onKeyDown={(e) => {
                  if (e.key.toLowerCase() === "enter") this.search();
                }}
                ref={this.searchBox}
                type="text"
                defaultValue={params.get("query") ? params.get("query") : ""}
                spellCheck={false}
                minLength={3}
                title="Keyword must contain at least 3 letters."
                placeholder="Search"
                disabled={isWaiting}
                className="data-field"
              />
              <button
                className="btn"
                disabled={isWaiting}
                onClick={(e) => {
                  this.search();
                }}
              >
                {Search}
              </button>
            </div>
            {isWaiting && (
              <>
                <div className="component-waiting center space-bet align-start column-flex">
                  <span className="loading-bar capsule"></span>
                  <span className="loading-bar capsule"></span>
                  <span className="loading-bar capsule"></span>
                </div>
              </>
            )}
          </div>
          <div className="data row center column-flex">
            {!isWaiting && query && (!data || data.length === 0) ? (
              <div className="row center layout-result">No results found</div>
            ) : (
              <></>
            )}
            <div className="content row">
              {data.map((elm) => (
                <Profile
                  key={elm.id}
                  type={2}
                  user={elm}
                  changeData={this.changeElementData}
                />
              ))}
            </div>
            {pages && pages !== 0 ? (
              <Pagination
                pages={pages}
                current={this.searchPage}
                callback={this.changePage}
              />
            ) : (
              <></>
            )}
          </div>
        </section>
      </>
    );
  }
}
