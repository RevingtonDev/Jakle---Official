import React, { Component } from "react";
import { Helmet } from "react-helmet";
import { useDispatch } from "react-redux";
import { useNavigate, useSearchParams } from "react-router-dom";
import { get_notifications } from "../api/request";
import { Notification } from "../components/Notification";
import { Pagination } from "../components/Pagination";
import { execute } from "../store";

export const Notifications = (props) => {
  let params = useSearchParams()[0];
  if (!params) params = new URLSearchParams();
  if (!params.get("page") || params.get("page") === "") params.set("page", "1");
  return (
    <NotificationsComponent
      {...props}
      params={params}
      dispatch={useDispatch()}
      navigate={useNavigate()}
      page={parseInt(params.get("page"))}
    />
  );
};

class NotificationsComponent extends Component {
  constructor(props) {
    super(props);

    this.state = {
      isWaiting: true,
      data: [],
      pages: 0,
      filter: 1,
    };

    this.page = this.props.page;

    this.navigate = this.navigate.bind(this);
  }

  componentDidMount() {
    this.props.dispatch(execute({ type: "notify", payload: false }));
    this.retrieveData();
  }

  componentDidUpdate() {
    if (this.props.page && this.props.page !== this.page) {
      this.page = this.props.page;
      this.retrieveData();
    }
  }

  async retrieveData() {
    this.setState({ isWaiting: true });
    const req = await get_notifications(
      undefined,
      this.page,
      15,
      this.state.filter
    );
    if (req) {
      this.setState({ data: req.results, pages: req.total_pages });
    }
    this.setState({ isWaiting: false });
  }

  navigate(page) {
    this.props.params.set("page", page);
    this.props.navigate({
      pathname: "./",
      search: this.props.params.toString(),
    });
  }

  changeFilter(filter) {
    this.setState({ filter: filter });
    this.retrieveData();
  }

  render() {
    const { data, pages, isWaiting, filter } = this.state;
    return (
      <>
        <Helmet>
          <title>Jakle - Notifications</title>
        </Helmet>
        <section className="page-container layout-page center justify-start column-flex">
          <div className="layout-title">Notifications</div>
          <div
            className={"data row " + (isWaiting ? "center" : "")}
            style={{ padding: "15px" }}
          >
            {isWaiting ? (
              <>
                <div className="component-waiting center space-bet align-start column-flex">
                  <span className="loading-bar capsule"></span>
                  <span className="loading-bar capsule"></span>
                  <span className="loading-bar capsule"></span>
                </div>
              </>
            ) : (
              <>
                <div className="row center row-flex">
                  <label htmlFor="filter" style={{ fontFamily: "lato" }}>
                    FIlter:
                  </label>
                  <select
                    disabled={isWaiting}
                    onChange={(e) => {
                      this.changeFilter(e.target.value);
                    }}
                    id="filter"
                    className="data-input"
                    defaultValue={filter}
                  >
                    <option className="data-field" value="1">
                      All
                    </option>
                    <option className="data-field" value="2">
                      Friend Requests
                    </option>
                  </select>
                </div>
                {!data || data.length === 0 ? (
                  <div className="row center layout-result">
                    No results found
                  </div>
                ) : (
                  <></>
                )}
                <div className="data-section content">
                  {data &&
                    data.map((elm) => <Notification notification={elm} />)}
                </div>
                {pages && pages !== 0 ? (
                  <Pagination
                    pages={pages}
                    current={this.page}
                    callback={this.navigate}
                  />
                ) : (
                  <></>
                )}
              </>
            )}
          </div>
        </section>
      </>
    );
  }
}
